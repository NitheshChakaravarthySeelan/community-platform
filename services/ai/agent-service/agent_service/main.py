import asyncio
import grpc
from concurrent import futures
import uuid
from typing import TypedDict, List, Dict, Any, Literal, Annotated
import operator

# Import the generated gRPC files
import agent_pb2
import agent_pb2_grpc
from google.protobuf import struct_pb2

# LangChain/LangGraph Modern Imports
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_mcp_adapters.tools import to_langchain_tools
from langgraph.prebuilt import create_react_agent
from langchain_core.messages import HumanMessage, ToolMessage, BaseMessage
from langgraph.graph import StateGraph, END
from langgraph.types import Command
from fastmcp import FastMCP
from pydantic import BaseModel, Field

# Helper function to validate UUID strings
def is_valid_uuid(uuid_string: str) -> bool:
    try:
        uuid.UUID(uuid_string, version=4)
        return True
    except ValueError:
        return False

def python_to_protobuf_value(data: Any) -> struct_pb2.Value:
    """Converts a Python object to a google.protobuf.Value."""
    if data is None:
        return struct_pb2.Value(null_value=struct_pb2.NULL_VALUE)
    elif isinstance(data, bool):
        return struct_pb2.Value(bool_value=data)
    elif isinstance(data, (int, float)):
        return struct_pb2.Value(number_value=float(data))
    elif isinstance(data, str):
        return struct_pb2.Value(string_value=data)
    elif isinstance(data, list):
        return struct_pb2.Value(list_value=struct_pb2.ListValue(values=[python_to_protobuf_value(v) for v in data]))
    elif isinstance(data, dict):
        return struct_pb2.Value(struct_value=struct_pb2.Struct(fields={k: python_to_protobuf_value(v) for k, v in data.items()}))
    else:
        return struct_pb2.Value(string_value=str(data))

# Client Imports
from agent_service.clients.product_lookup_client import product_lookup_client
from agent_service.clients.product_read_client import product_read_client
from agent_service.clients.cart_crud_client import cart_client
from agent_service.clients.checkout_client import checkout_client

# --- State Definition ---
class AgentState(TypedDict):
    messages: Annotated[List[BaseMessage], operator.add]
    user_id: str
    next_step: str

# --- Tools with Dynamic User Context ---
mcp = FastMCP(name="ShoppingAgent")

@mcp.tool
async def search_product(product_name: str) -> List[Dict[str, Any]]:
    """Searches for a product by name."""
    return await product_read_client.search_products(product_name)

@mcp.tool
async def get_product_details(product_id: str) -> Dict[str, Any]:
    """Gets details of a product by ID."""
    if not is_valid_uuid(product_id): return {"error": "Invalid ID"}
    return await product_lookup_client.get_product_by_id(product_id)

@mcp.tool
async def add_to_cart(product_id: str, quantity: int, user_id: str) -> Dict[str, Any]:
    """Adds a product to the user's cart."""
    if not is_valid_uuid(product_id): return {"error": "Invalid ID"}
    return await cart_client.add_item_to_cart(user_id=user_id, product_id=product_id, quantity=quantity)

@mcp.tool
async def view_cart(user_id: str) -> Dict[str, Any]:
    """Views the user's cart."""
    return await cart_client.get_cart(user_id=user_id)

@mcp.tool
async def checkout(user_id: str) -> Dict[str, Any]:
    """Initiates checkout for the user."""
    checkout_id = await checkout_client.initiate_checkout(user_id=user_id)
    return {"status": "initiated", "checkout_id": checkout_id}

# --- Multi-Agent Setup ---
llm = ChatGoogleGenerativeAI(model="gemini-1.5-flash")

# Workers
search_agent = create_react_agent(llm, [search_product, get_product_details])
cart_agent = create_react_agent(llm, [add_to_cart, view_cart, checkout])

# Supervisor Router
class Router(BaseModel):
    """Decide which specialist to delegate to."""
    next_agent: Literal["search_specialist", "cart_specialist", "FINISH"]
    instructions: str = Field(description="Instructions for the agent.")

def supervisor_node(state: AgentState):
    messages = state["messages"]
    # We force the supervisor to decide the next step
    router_llm = llm.with_structured_output(Router)
    decision = router_llm.invoke([
        HumanMessage(content=f"System Context: User ID is {state['user_id']}. Route the request to the right specialist."),
        *messages
    ])
    
    if decision.next_agent == "FINISH":
        return Command(goto=END)
    
    return Command(
        goto=decision.next_agent,
        update={"messages": [HumanMessage(content=decision.instructions, name="supervisor")]}
    )

# --- Graph Construction ---
builder = StateGraph(AgentState)
builder.add_node("supervisor", supervisor_node)
builder.add_node("search_specialist", search_agent)
builder.add_node("cart_specialist", cart_agent)

builder.set_entry_point("supervisor")
builder.add_edge("search_specialist", "supervisor")
builder.add_edge("cart_specialist", "supervisor")

app = builder.compile()

# --- gRPC Servicer ---
class AgentService(agent_pb2_grpc.AgentServiceServicer):
    async def ExecuteWorkflow(self, request, context):
        workflow_id = str(uuid.uuid4())
        user_id = request.user_id or "anonymous"
        
        initial_state = {
            "messages": [HumanMessage(content=request.user_query)],
            "user_id": user_id
        }

        yield agent_pb2.ExecuteWorkflowResponse(
            workflow_started=agent_pb2.WorkflowStartedEvent(workflow_id=workflow_id)
        )

        try:
            async for event in app.astream_events(initial_state, version="v2"):
                kind = event["event"]
                
                if kind == "on_tool_start":
                    # Inject user_id into tool calls if the tool expects it
                    # Note: LangGraph ReAct agent handles tool calling automatically, 
                    # but our tools now expect user_id. We rely on the LLM to pass it 
                    # based on the supervisor's instructions.
                    yield agent_pb2.ExecuteWorkflowResponse(
                        tool_started=agent_pb2.ToolStartedEvent(
                            tool_name=event["name"],
                            input=python_to_protobuf_value(event["data"].get("input"))
                        )
                    )
                elif kind == "on_tool_end":
                    yield agent_pb2.ExecuteWorkflowResponse(
                        tool_ended=agent_pb2.ToolEndedEvent(
                            tool_name=event["name"],
                            output=python_to_protobuf_value(event["data"].get("output"))
                        )
                    )
                elif kind == "on_chat_model_end":
                    message = event["data"]["output"]
                    # We only stream back messages from the supervisor or when ending
                    if not message.tool_calls and event["metadata"].get("langgraph_node") == "supervisor":
                        yield agent_pb2.ExecuteWorkflowResponse(
                            workflow_ended=agent_pb2.WorkflowEndedEvent(
                                final_response=message.content
                            )
                        )
                elif kind == "on_chain_error":
                    yield agent_pb2.ExecuteWorkflowResponse(
                        workflow_error=agent_pb2.WorkflowErrorEvent(
                            error_message=str(event["data"].get("error"))
                        )
                    )
        except Exception as e:
            yield agent_pb2.ExecuteWorkflowResponse(
                workflow_error=agent_pb2.WorkflowErrorEvent(error_message=str(e))
            )

async def serve():
    server = grpc.aio.server(futures.ThreadPoolExecutor(max_workers=10))
    agent_pb2_grpc.add_AgentServiceServicer_to_server(AgentService(), server)
    server.add_insecure_port('[::]:50050')
    await server.start()
    print("Server started on port 50050")
    await server.wait_for_termination()

if __name__ == '__main__':
    asyncio.run(serve())

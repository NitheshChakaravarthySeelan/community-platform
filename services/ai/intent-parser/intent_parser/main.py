from fastapi import FastAPI, HTTPException, Response # Add Response
import google.generativeai as genai
import os
import json
from pydantic import BaseModel, Field
from typing import Dict, Any, Optional, List
from prometheus_client import generate_latest, CollectorRegistry, Gauge # Import prometheus_client

from .api.schemas.intent import Intent, Action

# Create a registry to avoid clashes with other metrics
registry = CollectorRegistry()
# Example metrics:
# requests_in_progress = Gauge('http_requests_in_progress', 'Number of HTTP requests in progress', ['method', 'endpoint'], registry=registry)


class QueryRequest(BaseModel):
    query: str

# --- FastAPI App Setup ---
app = FastAPI(title="Intent Parser Service")

# --- Gemini API Configuration ---
GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")

if GOOGLE_API_KEY:
    genai.configure(api_key=GOOGLE_API_KEY)
    # Use a specific model if available, otherwise default to a general one
    # models_list = [m.name for m in genai.list_models()]
    # if "gemini-1.5-pro-latest" in models_list: # Check for the specific model
    #     llm_model = genai.GenerativeModel("gemini-1.5-pro-latest")
    # elif "gemini-pro" in models_list: # Fallback to gemini-pro if latest is not available
    #     llm_model = genai.GenerativeModel("gemini-pro")
    # else:
    #     raise RuntimeError("No suitable Gemini model found. Please check API key and model availability.")
    # For now, let's assume gemini-pro is always available
    llm_model = genai.GenerativeModel("gemini-pro")
else:
    llm_model = None # Handle cases where API key is not set

# --- API Endpoints ---
@app.get("/")
async def read_root():
    return {"message": "Hello from Intent Parser Service"}

# Prometheus Metrics Endpoint
@app.get("/metrics")
async def metrics():
    return Response(content=generate_latest(registry).decode("utf-8"), media_type="text/plain")

@app.get("/health")
async def health_check():
    return {"status": "ok"}

@app.post("/parse-intent", response_model=Intent)
async def parse_intent(request: QueryRequest):
    if not llm_model:
        raise HTTPException(status_code=503, detail="Gemini API key not configured.")

    system_instruction = """
    You are an AI assistant for an e-commerce platform. Your task is to interpret user queries
    and extract their intent into a structured JSON format. 
    
    The JSON must strictly adhere to the following Pydantic schema: 
    
    class Action(BaseModel):
        name: str = Field(..., description="Name of the action to be performed (e.g., add_to_cart, checkout, search_product, get_product_details, update_cart)")
        params: Dict[str, Any] = Field(default_factory=dict, description="Parameters for the action")

    class Intent(BaseModel):
        user_query: str = Field(..., description="The original natural language query from the user")
        action: Optional[Action] = Field(None, description="The primary action identified from the user's query")
        follow_up_questions: List[str] = Field(default_factory=list, description="Questions to ask the user for clarification or more information")
        confidence: float = Field(..., ge=0.0, le=1.0, description="Confidence score of the intent parsing")
        raw_llm_response: Dict[str, Any] = Field(default_factory=dict, description="Raw response from the LLM for debugging/auditing")

    
    Here are the supported actions and their expected parameters:

    1.  **add_to_cart**:
        -   `product_name` (str, required): The name of the product.
        -   `quantity` (int, default: 1): The number of items to add.
        -   `product_id` (str, optional): The ID of the product if specified.
    2.  **checkout**:
        -   No specific parameters needed, as it implies checking out the current cart.
    3.  **search_product**:
        -   `query` (str, required): The search term.
        -   `category` (str, optional): Filter by category.
    4.  **get_product_details**:
        -   `product_name` (str, required): The name of the product.
        -   `product_id` (str, optional): The ID of the product.
    5.  **update_cart**:
        -   `product_name` (str, required): The name of the product to update.
        -   `quantity` (int, required): The new quantity.
    6.  **remove_from_cart**:
        -   `product_name` (str, required): The name of the product to remove.
    7.  **view_cart**:
        -   No specific parameters.
    8.  **cancel_order**:
        -   `order_id` (str, required): The ID of the order to cancel.
    9.  **unknown**:
        -   No specific parameters. Use this if the intent cannot be clearly determined.

    If required parameters are missing for an action, formulate a `follow_up_questions` to ask the user.
    Always provide a `confidence` score between 0.0 and 1.0.
    Set `raw_llm_response` to the exact JSON output from this instruction.

    Example:
    User: "I want to buy a red t-shirt"
    Output:
    ```json
    {
      "user_query": "I want to buy a red t-shirt",
      "action": {
        "name": "search_product",
        "params": {
          "query": "red t-shirt"
        }
      },
      "follow_up_questions": [],
      "confidence": 0.95,
      "raw_llm_response": {{}}
    }
    ```
    Example for missing parameters:
    User: "Add to cart"
    Output:
    ```json
    {
      "user_query": "Add to cart",
      "action": {
        "name": "add_to_cart",
        "params": {}
      },
      "follow_up_questions": ["What product would you like to add?", "How many units?"],
      "confidence": 0.70,
      "raw_llm_response": {{}}
    }
    ```
    If the user's intent is ambiguous or cannot be mapped to any action, use the "unknown" action and ask clarifying questions.
    """.

    try:
        chat_session = llm_model.start_chat(history=[])
        response = chat_session.send_message(request.query + "\n\nOutput in JSON strictly following the schema:")
        
        # Extract JSON string from Gemini's response
        # Gemini might wrap JSON in markdown code block
        json_string = response.text.strip()
        if json_string.startswith("```json"):
            json_string = json_string[len("```json"):
].strip()
        if json_string.endswith("```"):
            json_string = json_string[:-len("```")].strip()

        parsed_response = json.loads(json_string)
        
        # Validate against Pydantic schema
        intent = Intent(
            user_query=request.query,
            action=Action(**parsed_response["action"]) if "action" in parsed_response and parsed_response["action"] else None,
            follow_up_questions=parsed_response.get("follow_up_questions", []),
            confidence=parsed_response.get("confidence", 0.0),
            raw_llm_response=parsed_response
        )
        return intent

    except json.JSONDecodeError as e:
        raise HTTPException(status_code=500, detail=f"Failed to parse LLM response: {e}. Raw response: {response.text}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error processing intent: {e}")
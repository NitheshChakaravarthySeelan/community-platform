from fastapi import FastAPI, HTTPException, Response # Add Response
from typing import List, Dict, Any
from prometheus_client import generate_latest, CollectorRegistry, Gauge # Import prometheus_client

from .api.schemas.intent import Intent, Action

# Create a registry to avoid clashes with other metrics
registry = CollectorRegistry()
# Example metrics:
# requests_in_progress = Gauge('http_requests_in_progress', 'Number of HTTP requests in progress', ['method', 'endpoint'], registry=registry)

app = FastAPI(title="Plan Generator Service")

@app.get("/")
async def read_root():
    return {"message": "Hello from Plan Generator Service"}

# Prometheus Metrics Endpoint
@app.get("/metrics")
async def metrics():
    return Response(content=generate_latest(registry).decode("utf-8"), media_type="text/plain")

@app.get("/health")
async def health_check():
    return {"status": "ok"}

@app.post("/generate-plan", response_model=List[Action])
async def generate_plan(intent: Intent):
    plan: List[Action] = []

    if intent.action:
        if intent.action.name == "add_to_cart":
            # Plan: Add to cart
            plan.append(Action(name="add_to_cart", params=intent.action.params))
        elif intent.action.name == "checkout":
            # Plan: Initiate checkout saga
            plan.append(Action(name="initiate_checkout_saga", params={}))
        elif intent.action.name == "search_product":
            # Plan: Search product
            plan.append(Action(name="search_product", params=intent.action.params))
        elif intent.action.name == "get_product_details":
            # Plan: Get product details
            plan.append(Action(name="get_product_details", params=intent.action.params))
        elif intent.action.name == "update_cart":
            # Plan: Update cart item
            plan.append(Action(name="update_cart_item", params=intent.action.params))
        elif intent.action.name == "remove_from_cart":
            # Plan: Remove from cart
            plan.append(Action(name="remove_from_cart", params=intent.action.params))
        elif intent.action.name == "view_cart":
            # Plan: View cart
            plan.append(Action(name="view_cart", params={}))
        elif intent.action.name == "cancel_order":
            # Plan: Cancel order
            plan.append(Action(name="cancel_order", params=intent.action.params))
        elif intent.action.name == "unknown":
            # If unknown, the plan might involve asking follow-up questions or default actions
            plan.append(Action(name="clarify_intent", params={"questions": intent.follow_up_questions}))
        else:
            raise HTTPException(status_code=400, detail=f"Unsupported action: {intent.action.name}")
    else:
        # If no primary action is identified, maybe a default search or clarification
        plan.append(Action(name="clarify_intent", params={"questions": ["What would you like to do?"]}))

    return plan
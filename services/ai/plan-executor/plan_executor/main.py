from fastapi import FastAPI, HTTPException, Response # Add Response
from typing import List, Dict, Any
import httpx
import os
import json
from prometheus_client import generate_latest, CollectorRegistry, Gauge # Import prometheus_client

from .api.schemas.intent import Intent, Action

# Create a registry to avoid clashes with other metrics
registry = CollectorRegistry()
# Example metrics:
# requests_in_progress = Gauge('http_requests_in_progress', 'Number of HTTP requests in progress', ['method', 'endpoint'], registry=registry)

app = FastAPI(title="Plan Executor Service")

# Base URLs for core services (these would come from a config service or env vars)
CART_CRUD_SERVICE_URL = os.getenv("CART_CRUD_SERVICE_URL", "http://localhost:3001")
CHECKOUT_ORCHESTRATOR_SERVICE_URL = os.getenv("CHECKOUT_ORCHESTRATOR_SERVICE_URL", "http://localhost:8000") # Use internal port for checkout_orchestrator
PRODUCT_READ_SERVICE_URL = os.getenv("PRODUCT_READ_SERVICE_URL", "http://localhost:8081")

@app.get("/")
async def read_root():
    return {"message": "Hello from Plan Executor Service"}

@app.get("/health")
async def health_check():
    return {"status": "ok"}

# Prometheus Metrics Endpoint
@app.get("/metrics")
async def metrics():
    return Response(content=generate_latest(registry).decode("utf-8"), media_type="text/plain")

@app.post("/execute-plan")
async def execute_plan(plan: List[Action]):
    results = []
    async with httpx.AsyncClient() as client:
        for action in plan:
            try:
                if action.name == "add_to_cart":
                    product_id = action.params.get("product_id")
                    product_name = action.params.get("product_name")
                    quantity = action.params.get("quantity", 1)

                    if not product_id and product_name:
                        # Try to find product by name
                        search_res = await client.get(f"{PRODUCT_READ_SERVICE_URL}/api/products/search", params={"query": product_name})
                        search_res.raise_for_status()
                        products = search_res.json()
                        if products:
                            product_id = products[0]["id"] # Take first result
                        else:
                            raise HTTPException(status_code=404, detail=f"Product '{product_name}' not found.")
                    
                    if not product_id:
                        raise HTTPException(status_code=400, detail="Product ID or name required for add_to_cart.")

                    response = await client.post(
                        f"{CART_CRUD_SERVICE_URL}/api/carts/add-item",
                        json={"productId": product_id, "quantity": quantity}
                    )
                    response.raise_for_status()
                    results.append({"action": action.name, "status": "success", "data": response.json()})

                elif action.name == "initiate_checkout_saga":
                    # This should eventually publish a Kafka event to checkout-orchestrator
                    # For now, directly call its API (which will then publish a Kafka event)
                    # Assuming checkout-orchestrator's /checkout expects cart_id and user_id
                    # These would typically come from context passed along with the plan
                    # For this example, we'll hardcode or assume default values
                    cart_id = action.params.get("cart_id", "default-cart-id")
                    user_id = action.params.get("user_id", "default-user-id")

                    response = await client.post(
                        f"{CHECKOUT_ORCHESTRATOR_SERVICE_URL}/api/checkout",
                        json={"cart_id": cart_id, "user_id": user_id}
                    )
                    response.raise_for_status()
                    results.append({"action": action.name, "status": "success", "data": response.json()})

                elif action.name == "search_product":
                    query = action.params.get("query")
                    category = action.params.get("category")
                    params = {"query": query}
                    if category:
                        params["category"] = category
                    response = await client.get(f"{PRODUCT_READ_SERVICE_URL}/api/products/search", params=params)
                    response.raise_for_status()
                    results.append({"action": action.name, "status": "success", "data": response.json()})

                elif action.name == "get_product_details":
                    product_id = action.params.get("product_id")
                    product_name = action.params.get("product_name")

                    if product_id:
                        url = f"{PRODUCT_READ_SERVICE_URL}/api/products/{product_id}"
                    elif product_name:
                        search_res = await client.get(f"{PRODUCT_READ_SERVICE_URL}/api/products/search", params={"query": product_name})
                        search_res.raise_for_status()
                        products = search_res.json()
                        if products:
                            url = f"{PRODUCT_READ_SERVICE_URL}/api/products/{products[0]['id']}"
                        else:
                            raise HTTPException(status_code=404, detail=f"Product '{product_name}' not found.")
                    else:
                        raise HTTPException(status_code=400, detail="Product ID or name required for get_product_details.")
                    
                    response = await client.get(url)
                    response.raise_for_status()
                    results.append({"action": action.name, "status": "success", "data": response.json()})
                
                # Add handlers for other actions like update_cart, remove_from_cart, view_cart, cancel_order
                elif action.name == "clarify_intent":
                    results.append({"action": action.name, "status": "info", "message": action.params.get("questions")})
                
                else:
                    results.append({"action": action.name, "status": "skipped", "message": f"Handler not implemented for action: {action.name}"})

            except HTTPException as e:
                results.append({"action": action.name, "status": "failed", "error": e.detail})
            except httpx.HTTPStatusError as e:
                results.append({"action": action.name, "status": "failed", "error": f"HTTP error {e.response.status_code}: {e.response.text}"})
            except Exception as e:
                results.append({"action": action.name, "status": "failed", "error": str(e)})

    return {"status": "Plan executed", "results": results}
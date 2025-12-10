import httpx # Keep httpx for now as it's still used in this version.
from databases import Database
from aiokafka import AIOKafkaProducer
from checkout_orchestrator.infrastructure.repositories.saga_repository import SagaRepository
from checkout_orchestrator.api.schemas.saga import SagaState
from checkout_orchestrator.api.schemas.checkout import CheckoutRequest
import datetime
import json
from typing import Dict, Any
import uuid # For generating saga IDs

# Import generated protobuf classes
# from checkout_orchestrator.product_pb2 import Product # Example
from checkout_orchestrator.catalog_events_pb2 import ProductUpdatedEvent, ProductCreatedEvent, ProductDeletedEvent # Example

# Define Kafka Topics (these would ideally be in a central config)
KAFKA_TOPIC_CHECKOUT_INITIATED = "checkout.checkout-initiated"
KAFKA_TOPIC_INVENTORY_COMMAND = "checkout.inventory-command"
KAFKA_TOPIC_PAYMENT_COMMAND = "checkout.payment-command"
KAFKA_TOPIC_ORDER_COMMAND = "checkout.order-command"
KAFKA_TOPIC_CART_COMMAND = "checkout.cart-command"
KAFKA_TOPIC_CHECKOUT_EVENTS = "checkout.checkout-events" # For events like InventoryReserved, PaymentProcessed, OrderCreated etc.

class CheckoutService:
    def __init__(
        self,
        database: Database,
        producer: AIOKafkaProducer,
        saga_repository: SagaRepository
    ):
        self.database = database
        self.producer = producer
        self.saga_repository = saga_repository
        # These URLs would come from a config service in a real app
        self.cart_service_url = "http://localhost:3001"
        self.inventory_service_url = "http://localhost:8085"
        self.payment_service_url = "http://localhost:8086"
        self.order_service_url = "http://localhost:8087"

    async def start_checkout_saga(self, cart_id: str, user_id: str) -> str:
        saga_id = str(uuid.uuid4()) # Generate a unique saga ID

        # 1. Get cart details - This will eventually be an event reaction, for now, direct call
        # In a real saga, the CheckoutInitiated event would typically carry enough info
        # or the cart service would emit CartCheckedOutEvent
        async with httpx.AsyncClient() as client:
            cart_response = await client.get(f"{self.cart_service_url}/api/carts/{cart_id}")
            cart_response.raise_for_status()
            cart_details = cart_response.json()

        # Initial saga context
        saga_context: Dict[str, Any] = {
            "cart_id": cart_id,
            "user_id": user_id,
            "cart_details": cart_details,
            "current_step": "CART_FETCHED",
            "errors": []
        }

        # Save initial saga state
        initial_saga_state = SagaState(
            id=saga_id,
            state="CHECKOUT_INITIATED",
            context=saga_context,
            created_at=datetime.datetime.now(datetime.timezone.utc),
            updated_at=datetime.datetime.now(datetime.timezone.utc),
        )
        await self.saga_repository.create(initial_saga_state)

        # Publish CheckoutInitiated event to Kafka
        # This event will kick off the first step of the saga (e.g., Inventory Reservation)
        # For simplicity, sending a dict as JSON. In real app, use Protobuf.
        checkout_initiated_payload = {
            "saga_id": saga_id,
            "user_id": user_id,
            "cart_id": cart_id,
            "cart_details": cart_details,
            "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat()
        }
        await self.producer.send_and_wait(
            KAFKA_TOPIC_CHECKOUT_INITIATED,
            json.dumps(checkout_initiated_payload).encode('utf-8')
        )
        print(f"Checkout saga {saga_id} initiated and event published.")

        return saga_id

    # The rest of the saga orchestration logic will be implemented in a Kafka consumer
    # that reacts to events like InventoryReserved, PaymentProcessed etc.
    async def perform_checkout(self, cart_id: str, user_id: str) -> dict:
        # This method will be removed/refactored as the saga takes over.
        # For now, it's left as a placeholder or to demonstrate the transition.
        raise NotImplementedError("perform_checkout is deprecated. Use start_checkout_saga instead.")

from aiokafka import AIOKafkaConsumer, AIOKafkaProducer
from databases import Database
from checkout_orchestrator.infrastructure.repositories.saga_repository import SagaRepository
from checkout_orchestrator.api.schemas.saga import SagaState
import asyncio
import logging
from typing import Dict, Any
import uuid
import datetime
from google.protobuf.timestamp_pb2 import Timestamp

# Import Protobuf generated classes
from .. import common_pb2
from .. import order_service_pb2
from .. import payment_service_pb2
from .. import wallet_service_pb2
from .. import invoice_service_pb2
from .. import inventory_pb2
from .. import checkout_events_pb2

logger = logging.getLogger(__name__)

# Topics
KAFKA_TOPIC_CHECKOUT_INITIATED = "checkout.checkout-initiated"
KAFKA_TOPIC_INVENTORY_COMMAND = "checkout.inventory-command"
KAFKA_TOPIC_PAYMENT_COMMAND = "checkout.payment-command"
KAFKA_TOPIC_WALLET_COMMAND = "checkout.wallet-command"
KAFKA_TOPIC_ORDER_COMMAND = "checkout.order-command"
KAFKA_TOPIC_INVOICE_COMMAND = "checkout.invoice-command"
KAFKA_TOPIC_CHECKOUT_EVENTS = "checkout.checkout-events"

class KafkaConsumerManager:
    def __init__(self, bootstrap_servers: str, database: Database, saga_repository: SagaRepository, producer: AIOKafkaProducer, httpx_client):
        self.consumer = AIOKafkaConsumer(
            KAFKA_TOPIC_CHECKOUT_INITIATED,
            KAFKA_TOPIC_CHECKOUT_EVENTS,
            bootstrap_servers=bootstrap_servers,
            group_id="checkout-orchestrator-group"
        )
        self.saga_repository = saga_repository
        self.producer = producer

    async def start_consumer(self):
        await self.consumer.start()
        try:
            async for msg in self.consumer:
                await self.process_message(msg)
        finally:
            await self.consumer.stop()

    async def process_message(self, msg):
        if msg.topic == KAFKA_TOPIC_CHECKOUT_INITIATED:
            import json
            data = json.loads(msg.value.decode('utf-8'))
            await self.start_saga(data)
            return

        # Attempt to parse as various events to route to handlers
        
        # 1. Inventory Events
        try:
            event = inventory_pb2.InventoryReservedEvent()
            event.ParseFromString(msg.value)
            if event.metadata.saga_id:
                await self.handle_inventory_reserved(event)
                return
        except: pass

        try:
            event = inventory_pb2.InventoryReservationFailedEvent()
            event.ParseFromString(msg.value)
            if event.metadata.saga_id:
                await self.handle_inventory_failed(event)
                return
        except: pass

        # 2. Payment Events
        try:
            event = payment_service_pb2.PaymentProcessedEvent()
            event.ParseFromString(msg.value)
            if event.metadata.saga_id:
                await self.handle_payment_processed(event)
                return
        except: pass

        try:
            event = payment_service_pb2.PaymentFailedEvent()
            event.ParseFromString(msg.value)
            if event.metadata.saga_id:
                await self.handle_payment_failed(event)
                return
        except: pass

        # 3. Order Events
        try:
            event = order_service_pb2.OrderCreatedEvent()
            event.ParseFromString(msg.value)
            if event.metadata.saga_id:
                await self.handle_order_created(event)
                return
        except: pass

        try:
            event = order_service_pb2.OrderCreationFailedEvent()
            event.ParseFromString(msg.value)
            if event.metadata.saga_id:
                await self.handle_order_failed(event)
                return
        except: pass
        # 4. Invoice Events
        try:
            event = invoice_service_pb2.InvoiceGeneratedEvent()
            event.ParseFromString(msg.value)
            if event.metadata.saga_id:
                await self.handle_invoice_generated(event)
                return
        except: pass

        try:
            event = invoice_service_pb2.InvoiceGenerationFailedEvent()
            event.ParseFromString(msg.value)
            if event.metadata.saga_id:
                await self.handle_invoice_failed(event)
                return
        except: pass

    async def start_saga(self, data):
        saga_id = data["saga_id"]
        logger.info(f"Saga {saga_id}: Initiating flow - Reserving Inventory")
        
        # Step 1: Reserve Inventory
        cmd = inventory_pb2.ReserveInventoryCommand()
        cmd.metadata.saga_id = saga_id
        cmd.user_id = data["user_id"]
        cmd.order_id = saga_id # Using saga_id as order_id for now
        
        for item in data["items"]:
            inv_item = cmd.items.add()
            inv_item.product_id = item["product_id"]
            inv_item.quantity = item["quantity"]
            
        await self.producer.send_and_wait(KAFKA_TOPIC_INVENTORY_COMMAND, cmd.SerializeToString())

    async def handle_inventory_reserved(self, event):
        saga_id = event.metadata.saga_id
        logger.info(f"Saga {saga_id}: Inventory Reserved. Triggering Payment.")
        
        # Fetch saga state to get total amount (in a real app, this would be in the context)
        saga = await self.saga_repository.get_by_id(saga_id)
        total_price_cents = saga.context.get("total_price_cents", 0)
        
        await self.trigger_payment(saga_id, event.user_id, total_price_cents)

    async def handle_inventory_failed(self, event):
        saga_id = event.metadata.saga_id
        logger.error(f"Saga {saga_id}: Inventory Reservation Failed. Reason: {event.reason}")
        # Update Saga State to FAILED
        await self.saga_repository.update_state(saga_id, "FAILED", {"error": event.reason})
        await self.emit_saga_failed(saga_id, event.user_id, event.reason, "INVENTORY_RESERVATION")

    async def trigger_payment(self, saga_id, user_id, amount_cents):
        cmd = payment_service_pb2.ProcessPaymentCommand()
        cmd.metadata.saga_id = saga_id
        cmd.user_id = user_id
        cmd.amount_cents = amount_cents
        cmd.order_id = saga_id 
        cmd.currency = "USD"
        cmd.payment_method = "CREDIT_CARD"
        
        await self.producer.send_and_wait(KAFKA_TOPIC_PAYMENT_COMMAND, cmd.SerializeToString())
        logger.info(f"Saga {saga_id}: Payment Command Sent")

    async def handle_payment_processed(self, event):
        saga_id = event.metadata.saga_id
        logger.info(f"Saga {saga_id}: Payment Successful. Creating Order.")
        
        cmd = order_service_pb2.CreateOrderCommand()
        cmd.metadata.saga_id = saga_id
        cmd.user_id = event.user_id
        cmd.total_cents = event.amount_cents
        
        # In a real app, we'd pass the items from the saga context here too
        await self.producer.send_and_wait(KAFKA_TOPIC_ORDER_COMMAND, cmd.SerializeToString())

    async def handle_payment_failed(self, event):
        saga_id = event.metadata.saga_id
        logger.error(f"Saga {saga_id}: Payment Failed. Reason: {event.reason}. Rolling back Inventory.")
        
        # Compensation: Release Inventory
        saga = await self.saga_repository.get_by_id(saga_id)
        items = saga.context.get("cart_details", {}).get("items", [])
        
        cmd = inventory_pb2.ReleaseInventoryCommand()
        cmd.metadata.saga_id = saga_id
        cmd.order_id = saga_id
        for item in items:
            inv_item = cmd.items.add()
            inv_item.product_id = item["product_id"]
            inv_item.quantity = item["quantity"]
            
        await self.producer.send_and_wait(KAFKA_TOPIC_INVENTORY_COMMAND, cmd.SerializeToString())
        await self.saga_repository.update_state(saga_id, "FAILED", {"error": f"Payment Failed: {event.reason}"})
        await self.emit_saga_failed(saga_id, event.user_id, event.reason, "PAYMENT_PROCESSING")

    async def handle_order_created(self, event):
        saga_id = event.metadata.saga_id
        logger.info(f"Saga {saga_id}: Order Created. Triggering Invoice.")
        
        cmd = invoice_service_pb2.GenerateInvoiceCommand()
        cmd.metadata.saga_id = saga_id
        cmd.order_id = event.order_id
        cmd.user_id = event.user_id
        cmd.total_cents = event.total_cents
        
        await self.producer.send_and_wait(KAFKA_TOPIC_INVOICE_COMMAND, cmd.SerializeToString())

    async def handle_order_failed(self, event):
        saga_id = event.metadata.saga_id
        logger.error(f"Saga {saga_id}: Order Creation Failed. Reason: {event.reason}. Rolling back Payment and Inventory.")
        
        # Compensation 1: Refund Payment
        refund_cmd = payment_service_pb2.RefundPaymentCommand()
        refund_cmd.metadata.saga_id = saga_id
        refund_cmd.reason = f"Order creation failed: {event.reason}"
        # In a real app, we'd need the transaction_id from the context
        await self.producer.send_and_wait(KAFKA_TOPIC_PAYMENT_COMMAND, refund_cmd.SerializeToString())
        
        # Compensation 2: Release Inventory
        saga = await self.saga_repository.get_by_id(saga_id)
        items = saga.context.get("cart_details", {}).get("items", [])
        inv_cmd = inventory_pb2.ReleaseInventoryCommand()
        inv_cmd.metadata.saga_id = saga_id
        inv_cmd.order_id = saga_id
        for item in items:
            inv_item = inv_cmd.items.add()
            inv_item.product_id = item["product_id"]
            inv_item.quantity = item["quantity"]
            
        await self.producer.send_and_wait(KAFKA_TOPIC_INVENTORY_COMMAND, inv_cmd.SerializeToString())
        await self.saga_repository.update_state(saga_id, "FAILED", {"error": f"Order Failed: {event.reason}"})
        await self.emit_saga_failed(saga_id, event.user_id, event.reason, "ORDER_CREATION")

    async def handle_invoice_generated(self, event):
        saga_id = event.metadata.saga_id
        logger.info(f"Saga {saga_id}: Invoice Generated. Saga COMPLETE.")
        
        saga = await self.saga_repository.get_by_id(saga_id)
        total_price_cents = saga.context.get("total_price_cents", 0)
        
        await self.saga_repository.update_state(saga_id, "COMPLETED")
        await self.emit_saga_completed(saga_id, saga.user_id, event.order_id, total_price_cents)

    async def handle_invoice_failed(self, event):
        saga_id = event.metadata.saga_id
        logger.error(f"Saga {saga_id}: Invoice Generation Failed. Reason: {event.reason}")
        # In a real app, we might decide if invoice failure warrants a full rollback
        # For now, we'll just mark the saga as FAILED but maybe the order still exists
        await self.saga_repository.update_state(saga_id, "FAILED", {"error": f"Invoice Failed: {event.reason}"})
        await self.emit_saga_failed(saga_id, "SYSTEM", event.reason, "INVOICE_GENERATION")

    async def emit_saga_completed(self, saga_id, user_id, order_id, total_price_cents):
        event = checkout_events_pb2.SagaCompletedEvent()
        event.saga_id = saga_id
        event.user_id = user_id
        event.order_id = order_id
        event.total_price_cents = total_price_cents
        
        now = datetime.datetime.now(datetime.timezone.utc)
        event.timestamp.FromDatetime(now)
        
        await self.producer.send_and_wait(KAFKA_TOPIC_CHECKOUT_EVENTS, event.SerializeToString())
        logger.info(f"Saga {saga_id}: Emitted SagaCompletedEvent")

    async def emit_saga_failed(self, saga_id, user_id, reason, failed_step):
        event = checkout_events_pb2.SagaFailedEvent()
        event.saga_id = saga_id
        event.user_id = user_id
        event.reason = reason
        event.failed_step = failed_step
        
        now = datetime.datetime.now(datetime.timezone.utc)
        event.timestamp.FromDatetime(now)
        
        await self.producer.send_and_wait(KAFKA_TOPIC_CHECKOUT_EVENTS, event.SerializeToString())
        logger.info(f"Saga {saga_id}: Emitted SagaFailedEvent")

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
from . import common_pb2
from . import order_service_pb2
from . import payment_service_pb2
from . import wallet_service_pb2
from . import invoice_service_pb2
from . import inventory_pb2
from . import checkout_events_pb2

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
        # In production, use headers or a wrapper message
        
        # Try PaymentProcessedEvent
        try:
            event = payment_service_pb2.PaymentProcessedEvent()
            event.ParseFromString(msg.value)
            if event.metadata.saga_id:
                await self.handle_payment_processed(event)
                return
        except: pass

        # Try OrderCreatedEvent
        try:
            event = order_service_pb2.OrderCreatedEvent()
            event.ParseFromString(msg.value)
            if event.metadata.saga_id:
                await self.handle_order_created(event)
                return
        except: pass

        # Try InvoiceGeneratedEvent
        try:
            event = invoice_service_pb2.InvoiceGeneratedEvent()
            event.ParseFromString(msg.value)
            if event.metadata.saga_id:
                await self.handle_invoice_generated(event)
                return
        except: pass

    async def start_saga(self, data):
        saga_id = data["saga_id"]
        logger.info(f"Saga {saga_id}: Initiating flow")
        
        # In a real app, first step is usually Inventory
        # For brevity, let's trigger Payment
        await self.trigger_payment(saga_id, data["user_id"], data["total_amount"])

    async def trigger_payment(self, saga_id, user_id, amount):
        cmd = payment_service_pb2.ProcessPaymentCommand()
        cmd.metadata.saga_id = saga_id
        cmd.user_id = user_id
        cmd.amount_cents = int(amount * 100)
        cmd.order_id = saga_id 
        cmd.currency = "USD"
        cmd.payment_method = "CREDIT_CARD"
        
        await self.producer.send_and_wait(KAFKA_TOPIC_PAYMENT_COMMAND, cmd.SerializeToString())
        logger.info(f"Saga {saga_id}: Payment Command Sent")

    async def handle_payment_processed(self, event):
        saga_id = event.metadata.saga_id
        logger.info(f"Saga {saga_id}: Payment Successful. Triggering Wallet Debit.")
        
        cmd = wallet_service_pb2.DebitWalletCommand()
        cmd.metadata.saga_id = saga_id
        cmd.user_id = event.user_id
        cmd.amount_cents = event.amount_cents
        cmd.reason = "Order Payment"
        
        await self.producer.send_and_wait(KAFKA_TOPIC_WALLET_COMMAND, cmd.SerializeToString())

    async def handle_order_created(self, event):
        saga_id = event.metadata.saga_id
        logger.info(f"Saga {saga_id}: Order Created. Triggering Invoice.")
        
        cmd = invoice_service_pb2.GenerateInvoiceCommand()
        cmd.metadata.saga_id = saga_id
        cmd.order_id = event.order_id
        cmd.user_id = event.user_id
        cmd.total_cents = event.total_cents
        
        await self.producer.send_and_wait(KAFKA_TOPIC_INVOICE_COMMAND, cmd.SerializeToString())

    async def handle_invoice_generated(self, event):
        saga_id = event.metadata.saga_id
        logger.info(f"Saga {saga_id}: Invoice Generated. Saga COMPLETE.")
        # Update Saga State in DB to COMPLETED

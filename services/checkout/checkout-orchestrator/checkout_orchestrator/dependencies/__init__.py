from databases import Database
from aiokafka import AIOKafkaProducer
import httpx
from ..core.services.checkout_service import CheckoutService
from ..infrastructure.repositories.saga_repository import SagaRepository
from ..core.config import database, kafka_producer, httpx_client # Import the shared instances

async def get_database() -> Database:
    return database

async def get_kafka_producer() -> AIOKafkaProducer:
    return kafka_producer

async def get_saga_repository() -> SagaRepository:
    # SagaRepository now always uses the global database instance
    repo = SagaRepository(database)
    # create_saga_table is now called in main.py startup event
    return repo

async def get_checkout_service():
    # Use global instances from config
    db = database
    producer = kafka_producer
    saga_repo = await get_saga_repository()
    client = httpx_client

    return CheckoutService(db, producer, saga_repo, client) # Pass client to CheckoutService
from databases import Database
from aiokafka import AIOKafkaProducer
from .core.services.checkout_service import CheckoutService
from .infrastructure.repositories.saga_repository import SagaRepository
from .main import database, kafka_producer # Import the shared instances

async def get_database() -> Database:
    return database

async def get_kafka_producer() -> AIOKafkaProducer:
    return kafka_producer

async def get_saga_repository() -> SagaRepository:
    # SagaRepository now always uses the global database instance
    repo = SagaRepository(database)
    # create_saga_table is now called in main.py startup event
    return repo

async def get_checkout_service(
    db: Database = None,
    producer: AIOKafkaProducer = None,
    saga_repo: SagaRepository = None
) -> CheckoutService:
    # Use global instances if not provided (expected in actual app run)
    if db is None:
        db = database
    if producer is None:
        producer = kafka_producer
    if saga_repo is None:
        saga_repo = await get_saga_repository()
    
    return CheckoutService(db, producer, saga_repo)
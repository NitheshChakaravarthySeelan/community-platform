from pydantic import BaseModel
from typing import Any, Dict, List
import datetime

class SagaState(BaseModel):
    id: str # Corresponds to saga_id, e.g., cart_id or order_id
    state: str
    context: Dict[str, Any]
    processed_event_ids: List[str] = [] # New field for idempotency
    created_at: datetime.datetime
    updated_at: datetime.datetime
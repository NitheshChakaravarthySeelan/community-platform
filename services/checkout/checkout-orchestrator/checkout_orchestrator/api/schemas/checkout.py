from pydantic import BaseModel
from typing import Dict, Any, Optional

class CheckoutRequest(BaseModel):
    cart_id: str
    user_id: str
    cart_details: Dict[str, Any]

class CheckoutResponse(BaseModel):
    success: bool
    order_id: Optional[str] = None
    message: Optional[str] = None

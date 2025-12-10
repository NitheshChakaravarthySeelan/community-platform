from pydantic import BaseModel

class CheckoutRequest(BaseModel):
    cart_id: str
    user_id: str

class CheckoutResponse(BaseModel):
    success: bool
    order_id: str | None = None
    message: str | None = None

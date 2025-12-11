from fastapi import APIRouter, Depends, HTTPException
from ..schemas.checkout import CheckoutRequest, CheckoutResponse
from ...core.services.checkout_service import CheckoutService
from ...dependencies import get_checkout_service

router = APIRouter()

@router.post("/checkout", response_model=CheckoutResponse)
async def checkout(
    request: CheckoutRequest,
    checkout_service: CheckoutService = Depends(get_checkout_service)
):
    try:
        saga_id = await checkout_service.start_checkout_saga(request.cart_id, request.user_id, request.cart_details)
        return CheckoutResponse(
            success=True,
            order_id=saga_id, # Returning saga_id instead of order_id
            message="Checkout saga initiated"
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
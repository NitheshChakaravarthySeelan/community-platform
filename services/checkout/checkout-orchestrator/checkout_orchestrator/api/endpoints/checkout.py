from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import JSONResponse
from ..schemas.checkout import CheckoutRequest, CheckoutResponse
from ...core.services.checkout_service import CheckoutService
from ...dependencies import get_checkout_service
from checkout_orchestrator.utils.uuid_utils import is_valid_uuid # Import the uuid validation utility

router = APIRouter()

@router.post("/checkout")
async def checkout(
    request: CheckoutRequest,
    checkout_service: CheckoutService = Depends(get_checkout_service)
) -> dict:
    if not is_valid_uuid(request.user_id):
        raise HTTPException(status_code=400, detail="Invalid user_id format.")
    if not is_valid_uuid(request.cart_id):
        raise HTTPException(status_code=400, detail="Invalid cart_id format.")

    try:
        saga_id = await checkout_service.start_checkout_saga(
            cart_id=request.cart_id,
            user_id=request.user_id,
            cart_details=request.cart_details
        )
        return {
            "success": True,
            "order_id": saga_id,
            "message": "Checkout saga initiated successfully."
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
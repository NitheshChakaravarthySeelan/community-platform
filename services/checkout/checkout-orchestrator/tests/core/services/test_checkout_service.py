import pytest
import respx
from httpx import Response
from checkout_orchestrator.core.services.checkout_service import CheckoutService

@pytest.mark.asyncio
@respx.mock
async def test_perform_checkout_success():
    # Arrange
    cart_id = "cart-123"
    user_id = "user-123"

    cart_service_url = "http://localhost:8084"
    inventory_service_url = "http://localhost:8085"
    payment_service_url = "http://localhost:8086"
    order_service_url = "http://localhost:8087"

    # Mock external service calls
    respx.get(f"{cart_service_url}/carts/{cart_id}").mock(
        return_value=Response(200, json={"items": [{"product_id": "prod-1", "quantity": 1}], "total_price": 100})
    )
    respx.get(f"{inventory_service_url}/inventory/prod-1").mock(
        return_value=Response(200, json={"stock": 10})
    )
    respx.post(f"{payment_service_url}/payments").mock(
        return_value=Response(200)
    )
    respx.post(f"{order_service_url}/orders").mock(
        return_value=Response(201, json={"id": "order-abc"})
    )
    respx.delete(f"{cart_service_url}/carts/{cart_id}").mock(
        return_value=Response(204)
    )

    service = CheckoutService()

    # Act
    order = await service.perform_checkout(cart_id, user_id)

    # Assert
    assert order["id"] == "order-abc"

@pytest.mark.asyncio
@respx.mock
async def test_perform_checkout_insufficient_stock():
    # Arrange
    cart_id = "cart-456"
    user_id = "user-456"

    cart_service_url = "http://localhost:8084"
    inventory_service_url = "http://localhost:8085"

    respx.get(f"{cart_service_url}/carts/{cart_id}").mock(
        return_value=Response(200, json={"items": [{"product_id": "prod-2", "quantity": 5}]})
    )
    respx.get(f"{inventory_service_url}/inventory/prod-2").mock(
        return_value=Response(200, json={"stock": 4})
    )

    service = CheckoutService()

    # Act & Assert
    with pytest.raises(Exception, match="Not enough stock"):
        await service.perform_checkout(cart_id, user_id)

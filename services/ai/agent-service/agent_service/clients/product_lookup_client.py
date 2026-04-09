import grpc
import os
import product_lookup_pb2
import product_lookup_pb2_grpc

class ProductLookupClient:
    def __init__(self):
        self.host = os.getenv("PRODUCT_LOOKUP_HOST", "localhost")
        self.port = os.getenv("PRODUCT_LOOKUP_PORT", "50051")
        self.channel = grpc.aio.insecure_channel(f'{self.host}:{self.port}')
        self.client = product_lookup_pb2_grpc.ProductLookupStub(self.channel)

    async def get_product_by_id(self, product_id: str) -> dict | None:
        request = product_lookup_pb2.GetProductByIdRequest(id=product_id)
        try:
            response = await self.client.GetProductById(request)
            product_dict = self.convert_request_to_product(response)
            return {k: v for k, v in product_dict.items() if v}
        except grpc.aio.AioRpcError as e:
            print(f"Error calling ProductLookup.GetProductById for ID {product_id}: {e}")
            return None
        except Exception as e:
            print(f"An unexpected error occured: {e}")
            return None

    def convert_request_to_product(self, response):
        return {
            "id" : response.id,
            "name": response.name,
            "description": response.description,
            "price": response.price,
            "quantity": response.quantity,
            "sku": response.sku,
            "image_url": response.image_url,
            "category": response.category,
            "manufacturer": response.manufacturer,
            "status": response.status,
            "version": response.version,
            "created_at": response.created_at,
            "updated_at": response.updated_at,
        }

product_lookup_client = ProductLookupClient()

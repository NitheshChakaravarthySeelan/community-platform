use tonic::{Request, Response, Status};
use product_lookup_rust::{MyProductLookup, ProductRow, ProductRepository, product_lookup::{GetProductByIdRequest, Product, product_lookup_server::ProductLookup}};
use async_trait::async_trait;
use std::sync::Arc;
use chrono::{Utc, TimeZone};

// Mock implementation of ProductRepository for testing
pub struct MockProductRepository {
    // You could add fields here to control the behavior of the mock
    // e.g., expected_id: Option<i64>, return_value: Option<ProductRow>, etc.
}

#[async_trait]
impl ProductRepository for MockProductRepository {
    async fn find_product_by_id(&self, id: i64) -> Result<Option<ProductRow>, sqlx::Error> {
        match id {
            1 => Ok(Some(ProductRow {
                id: 1,
                name: Some("Test Product".to_string()),
                description: Some("A test product".to_string()),
                price: Some(9.99),
                quantity: Some(10),
                sku: Some("TEST-001".to_string()),
                image_url: Some("http://example.com/test.jpg".to_string()),
                category: Some("Electronics".to_string()),
                manufacturer: Some("TestCorp".to_string()),
                status: Some("ACTIVE".to_string()),
                version: Some(1),
                created_at: Utc.timestamp_opt(1678886400, 0).unwrap(),
                updated_at: Utc.timestamp_opt(1678886400, 0).unwrap(),
            })),
            _ => Ok(None), // Simulate product not found for other IDs
        }
    }
}

#[tokio::test]
async fn test_get_product_by_id_success() -> Result<(), Box<dyn std::error::Error>> {
    // 1. Setup mock repository
    let mock_repo = Arc::new(MockProductRepository {});
    let lookup_service = MyProductLookup::new(mock_repo);

    // 2. Prepare request
    let request = Request::new(GetProductByIdRequest {
        id: "1".to_string(),
    });

    // 3. Call the service method
    let response = lookup_service.get_product_by_id(request).await?;

    // 4. Assertions
    let product = response.into_inner();
    assert_eq!(product.id, "1");
    assert_eq!(product.name, "Test Product");
    assert_eq!(product.price, 9.99);
    assert_eq!(product.sku, "TEST-001");
    // Add more assertions for other fields
    
    Ok(())
}

#[tokio::test]
async fn test_get_product_by_id_not_found() -> Result<(), Box<dyn std::error::Error>> {
    // 1. Setup mock repository
    let mock_repo = Arc::new(MockProductRepository {});
    let lookup_service = MyProductLookup::new(mock_repo);

    // 2. Prepare request
    let request = Request::new(GetProductByIdRequest {
        id: "999".to_string(), // Non-existent ID
    });

    // 3. Call the service method and expect an error
    let error = lookup_service.get_product_by_id(request).await.unwrap_err();

    // 4. Assertions
    assert_eq!(error.code(), Status::not_found("").code());
    assert!(error.message().contains("Product with ID 999 not found"));

    Ok(())
}

#[tokio::test]
async fn test_get_product_by_id_invalid_id() -> Result<(), Box<dyn std::error::Error>> {
    // 1. Setup mock repository (not used for this test's logic, but required for MyProductLookup)
    let mock_repo = Arc::new(MockProductRepository {});
    let lookup_service = MyProductLookup::new(mock_repo);

    // 2. Prepare request
    let request = Request::new(GetProductByIdRequest {
        id: "invalid-id".to_string(),
    });

    // 3. Call the service method and expect an error
    let error = lookup_service.get_product_by_id(request).await.unwrap_err();

    // 4. Assertions
    assert_eq!(error.code(), Status::invalid_argument("").code());
    assert!(error.message().contains("Invalid product ID"));

    Ok(())
}

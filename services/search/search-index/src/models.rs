use serde::{Deserialize, Serialize};
use crate::catalog_events::{ProductCreatedEvent, ProductUpdatedEvent};

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)]
pub struct ProductDocument {
    pub id: String,
    pub name: String,
    pub description: String,
    pub price: f64,
    pub quantity: i32,
    pub sku: String,
    pub image_url: String,
    pub category: String,
    pub manufacturer: String,
    pub status: String,
}

impl From<ProductCreatedEvent> for ProductDocument {
    fn from(event: ProductCreatedEvent) -> Self {
        Self {
            id: event.product_id,
            name: event.name,
            description: event.description,
            price: event.price,
            quantity: event.quantity,
            sku: event.sku,
            image_url: event.image_url,
            category: event.category,
            manufacturer: event.manufacturer,
            status: event.status,
        }
    }
}

impl From<ProductUpdatedEvent> for ProductDocument {
    fn from(event: ProductUpdatedEvent) -> Self {
        Self {
            id: event.product_id,
            name: event.name,
            description: event.description,
            price: event.price,
            quantity: event.quantity,
            sku: event.sku,
            image_url: event.image_url,
            category: event.category,
            manufacturer: event.manufacturer,
            status: event.status,
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::catalog_events::ProductCreatedEvent;

    #[test]
    fn test_product_document_mapping() {
        let event = ProductCreatedEvent {
            product_id: "123".to_string(),
            name: "Test Product".to_string(),
            description: "A test description".to_string(),
            price: 99.99,
            quantity: 10,
            sku: "TEST-SKU-001".to_string(),
            image_url: "http://example.com/image.jpg".to_string(),
            category: "Electronics".to_string(),
            manufacturer: "Test Corp".to_string(),
            status: "active".to_string(),
            version: 1,
            created_at: "2024-03-20T10:00:00Z".to_string(),
        };

        let doc: ProductDocument = ProductDocument::from(event);
        assert_eq!(doc.id, "123");
        assert_eq!(doc.name, "Test Product");
        assert_eq!(doc.price, 99.99);
    }
}

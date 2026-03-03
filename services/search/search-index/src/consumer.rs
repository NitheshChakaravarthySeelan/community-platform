use meilisearch_sdk::indexes::Index;
use rdkafka::config::ClientConfig;
use rdkafka::consumer::{Consumer, StreamConsumer};
use rdkafka::message::{Headers, Message};
use std::sync::Arc;
use tokio_stream::StreamExt;
use tracing::{error, info, warn};
use prost::Message as ProstMessage;

use crate::config::Config;
use crate::models::ProductDocument;
use crate::catalog_events::{ProductCreatedEvent, ProductUpdatedEvent, ProductDeletedEvent};

pub async fn run_consumer(config: Config, meilisearch_index: Arc<Index>) -> Result<(), Box<dyn std::error::Error>> {
    let consumer: StreamConsumer = ClientConfig::new()
        .set("bootstrap.servers", &config.kafka_brokers)
        .set("group.id", &config.kafka_group_id)
        .set("enable.auto.commit", "true")
        .set("auto.offset.reset", "earliest")
        .create()
        .expect("Consumer creation failed");

    consumer.subscribe(&[&config.kafka_topic]).expect("Can't subscribe to specified topic");
    info!("Subscribed to Kafka topic '{}'", config.kafka_topic);

    let mut message_stream = consumer.stream();

    info!("Entering consumption loop...");
    while let Some(message_result) = message_stream.next().await {
        match message_result {
            Err(e) => error!("Kafka error: {}", e),
            Ok(borrowed_message) => {
                let payload = match borrowed_message.payload() {
                    None => continue,
                    Some(p) => p,
                };

                let event_type = borrowed_message.headers().and_then(|h| {
                    for i in 0..h.count() {
                        let header = h.get(i);
                        if header.key == "event_type" {
                            return header.value.and_then(|v| std::str::from_utf8(v).ok());
                        }
                    }
                    None
                });

                if let Some(et) = event_type {
                    match et {
                        "ProductCreated" => {
                            if let Ok(event) = ProductCreatedEvent::decode(payload) {
                                let doc = ProductDocument::from(event);
                                match meilisearch_index.add_documents(&[doc], Some("id")).await {
                                    Ok(_) => info!("Indexed new product: {}", et),
                                    Err(e) => error!("Meilisearch error: {}", e),
                                }
                            }
                        },
                        "ProductUpdated" => {
                            if let Ok(event) = ProductUpdatedEvent::decode(payload) {
                                let doc = ProductDocument::from(event);
                                match meilisearch_index.add_documents(&[doc], Some("id")).await {
                                    Ok(_) => info!("Updated product: {}", et),
                                    Err(e) => error!("Meilisearch error: {}", e),
                                }
                            }
                        },
                        "ProductDeleted" => {
                            if let Ok(event) = ProductDeletedEvent::decode(payload) {
                                match meilisearch_index.delete_document(&event.product_id).await {
                                    Ok(_) => info!("Deleted product: {}", event.product_id),
                                    Err(e) => error!("Meilisearch error: {}", e),
                                }
                            }
                        },
                        _ => warn!("Unknown event type: {}", et),
                    }
                } else {
                    // Fallback heuristic if headers are missing
                    if let Ok(event) = ProductCreatedEvent::decode(payload) {
                        let doc = ProductDocument::from(event);
                        let _ = meilisearch_index.add_documents(&[doc], Some("id")).await;
                        info!("Processed fallback ProductCreated");
                    } else if let Ok(event) = ProductUpdatedEvent::decode(payload) {
                        let doc = ProductDocument::from(event);
                        let _ = meilisearch_index.add_documents(&[doc], Some("id")).await;
                        info!("Processed fallback ProductUpdated");
                    } else if let Ok(event) = ProductDeletedEvent::decode(payload) {
                        let _ = meilisearch_index.delete_document(&event.product_id).await;
                        info!("Processed fallback ProductDeleted");
                    }
                }
            }
        }
    }

    Ok(())
}

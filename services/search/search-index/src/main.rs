use std::sync::Arc;
use meilisearch_sdk::client::Client;
use tracing::info;

pub mod config;
pub mod models;
pub mod consumer;

// Import the generated Rust code from Protobuf
pub mod catalog_events {
    include!(concat!(env!("OUT_DIR"), "/catalog_events.rs"));
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    // 1. Initialize Logging
    tracing_subscriber::fmt::init();
    info!("Starting Search Index Service...");

    // 2. Load Configuration
    let config = config::Config::from_env();
    info!("Configuration loaded for Kafka brokers: {}", config.kafka_brokers);

    // 3. Initialize Meilisearch Client
    let client = Client::new(&config.meilisearch_url, Some(&config.meilisearch_api_key))?;
    let index = Arc::new(client.index(&config.meilisearch_index));
    info!("Meilisearch client initialized for index '{}'", config.meilisearch_index);

    // 4. Run Consumer Loop
    consumer::run_consumer(config, index).await?;

    Ok(())
}

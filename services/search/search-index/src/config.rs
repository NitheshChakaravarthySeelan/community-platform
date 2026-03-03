use std::env;

#[derive(Debug, Clone)]
pub struct Config {
    pub kafka_brokers: String,
    pub kafka_topic: String,
    pub kafka_group_id: String,
    pub meilisearch_url: String,
    pub meilisearch_api_key: String,
    pub meilisearch_index: String,
}

impl Config {
    pub fn from_env() -> Self {
        Self {
            kafka_brokers: env::var("KAFKA_BOOTSTRAP_SERVERS")
                .unwrap_or_else(|_| env::var("KAFKA_BROKERS").expect("KAFKA_BROKERS or KAFKA_BOOTSTRAP_SERVERS must be set")),
            kafka_topic: env::var("PRODUCT_EVENTS_TOPIC")
                .unwrap_or_else(|_| env::var("KAFKA_TOPIC").expect("KAFKA_TOPIC or PRODUCT_EVENTS_TOPIC must be set")),
            kafka_group_id: env::var("KAFKA_GROUP_ID").expect("KAFKA_GROUP_ID must be set"),
            meilisearch_url: env::var("MEILISEARCH_URL").expect("MEILISEARCH_URL must be set"),
            meilisearch_api_key: env::var("MEILISEARCH_API_KEY").expect("MEILISEARCH_API_KEY must be set"),
            meilisearch_index: env::var("MEILISEARCH_INDEX").expect("MEILISEARCH_INDEX must be set"),
        }
    }
}

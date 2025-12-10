// services/inventory/inventory-write/src/telemetry.rs

use actix_web::middleware::Logger;
use metrics_exporter_prometheus::PrometheusBuilder;
use tracing_actix_web::TracingLogger;
use tracing_bunyan_formatter::{BunyanFormattingLayer, JsonStorageLayer};
use tracing_subscriber::{EnvFilter, Registry};
use tracing_subscriber::layer::SubscriberExt;
use tracing_subscriber::util::SubscriberInitExt;

/// Initializes a tracing subscriber for structured logging.
pub fn init_subscriber(service_name: String, env_filter: String) {
    let env_filter = EnvFilter::try_from_default_env()
        .unwrap_or_else(|_| EnvFilter::new(env_filter));

    let formatting_layer = BunyanFormattingLayer::new(service_name, std::io::stdout);

    Registry::default()
        .with(env_filter)
        .with(JsonStorageLayer)
        .with(formatting_layer)
        .init();
}

/// Sets up the Prometheus metrics recorder and returns Actix-web middleware.
pub fn setup_metrics_recorder() -> metrics_exporter_prometheus::PrometheusMetrics {
    let builder = PrometheusBuilder::new().add_service_endpoint("/metrics");
    let recorder = builder.build().unwrap(); // Build the Prometheus recorder
    recorder
}

// Re-export TracingLogger for use in App::new().wrap()
pub use tracing_actix_web::TracingLogger;
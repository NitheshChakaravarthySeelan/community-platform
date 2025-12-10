// services/inventory/inventory-read/src/main.rs
// Main binary entrypoint.

use actix_web::{web, App, HttpServer};
use dotenvy::dotenv;
use sqlx::PgPool;
use std::env;

use inventory_read::api::{health, inventory};
use inventory_read::domain::service::InventoryService;
use inventory_read::telemetry::{init_subscriber, setup_metrics_recorder};

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    dotenv().ok();
    init_subscriber("inventory-read".into(), "info".into()); // Initialize tracing subscriber
    let recorder_handle = setup_metrics_recorder(); // Setup Prometheus metrics recorder

    println!("Starting inventory-read service...");

    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL must be set");
    let pool = PgPool::connect(&database_url)
        .await
        .expect("Failed to connect to Postgres.");

    let inventory_service = InventoryService::new(pool.clone());

    HttpServer::new(move || {
        App::new()
            .app_data(web::Data::new(recorder_handle.clone())) // Pass PrometheusHandle to be accessible by handlers
            .app_data(web::Data::new(pool.clone())) // Pass pool to be accessible by handlers
            .app_data(web::Data::new(inventory_service.clone())) // Pass InventoryService
            .service(health::health_check) // Register health check route
            .route("/metrics", web::get().to(health::prometheus_metrics_handler)) // Add metrics route
            .route("/inventory/{product_id}", web::get().to(inventory::get_inventory_by_product_id)) // Register inventory route
    })
    .bind(("0.0.0.0", 8080))? // Use port 8080 by default
    .run()
    .await
}

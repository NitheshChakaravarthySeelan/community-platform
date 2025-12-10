// services/inventory/inventory-write/src/main.rs
// Main binary entrypoint.

use actix_web::{web, App, HttpServer};
use dotenvy::dotenv;
use sqlx::PgPool;
use std::env;

use inventory_write::api::{health, inventory};
use inventory_write::domain::service::InventoryService;
use inventory_write::telemetry::{init_subscriber, setup_metrics_recorder};
use tracing_actix_web::TracingLogger;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    dotenv().ok();
    init_subscriber("inventory-write".into(), "info".into()); // Initialize tracing subscriber
    let recorder = setup_metrics_recorder(); // Setup Prometheus metrics recorder

    println!("Starting inventory-write service...");

    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL must be set");
    let pool = PgPool::connect(&database_url)
        .await
        .expect("Failed to connect to Postgres.");

    let inventory_service = InventoryService::new(pool.clone());

    HttpServer::new(move || {
        App::new()
            .wrap(TracingLogger::default()) // Add tracing for requests
            .wrap(recorder.clone()) // Apply Prometheus middleware
            .app_data(web::Data::new(pool.clone())) // Pass pool to be accessible by handlers
            .app_data(web::Data::new(inventory_service.clone())) // Pass InventoryService
            .service(health::health_check) // Register health check route
            .route("/inventory/{product_id}/update", web::post().to(inventory::update_inventory)) // Register inventory update route
    })
    .bind(("0.0.0.0", 8080))? // Use port 8080 by default
    .run()
    .await
}

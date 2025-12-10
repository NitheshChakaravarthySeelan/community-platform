// services/inventory/inventory-write/src/api/health.rs
use actix_web::{get, HttpResponse, Responder};

#[get("/health")]
pub async fn health_check() -> impl Responder {
    HttpResponse::Ok().body("Service is healthy!")
}
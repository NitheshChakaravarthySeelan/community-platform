// services/inventory/inventory-read/src/api/health.rs
use actix_web::{get, HttpResponse, Responder};
use metrics_exporter_prometheus::PrometheusHandle;

#[get("/health")]
pub async fn health_check() -> impl Responder {
    HttpResponse::Ok().body("Service is healthy!")
}

pub async fn prometheus_metrics_handler(recorder_handle: actix_web::web::Data<PrometheusHandle>) -> impl Responder {
    recorder_handle.render()
}
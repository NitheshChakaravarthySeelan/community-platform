// services/inventory/inventory-write/src/lib.rs
// Library root. Declares all other modules.

pub mod api;
pub mod config;
pub mod domain;
pub mod error;
pub mod infrastructure;
pub mod telemetry;
pub mod kafka_consumer;
pub mod events;

pub mod catalog_events {
    include!(concat!(env!("OUT_DIR"), "/catalog_events.rs"));
}

pub mod inventory {
    include!(concat!(env!("OUT_DIR"), "/inventory.rs"));
}

pub mod common {
    include!(concat!(env!("OUT_DIR"), "/common.rs"));
}



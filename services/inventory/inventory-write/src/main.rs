// services/inventory/inventory-write/src/main.rs
// Main binary entrypoint.
// Its only job is to initialize and run the application defined in `lib.rs`.
use log::{info, warn};
use config::Config::from_env();

fn main() {
    match from_env() {
        Ok()
    }
}

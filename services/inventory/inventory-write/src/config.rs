// services/inventory/inventory-write/src/config.rs

use serde::Deserialize;
use sqlx::postgres::PgConnectOptions;
use std::str::FromStr; // Import FromStr trait
use config::{Config as ExternalConfig, File, Environment}; // Use alias to avoid name collision

#[derive(Debug, Deserialize)]
pub struct Config {
    pub database: DatabaseConfig,
    pub application: ApplicationConfig,
}

#[derive(Debug, Deserialize)]
pub struct DatabaseConfig {
    pub host: String,
    pub port: u16,
    pub username: String,
    pub password: String,
    pub database_name: String,
}

impl DatabaseConfig {
    // Helper function to create sqlx connection options
    pub fn with_db(&self) -> PgConnectOptions {
        self.without_db().database(&self.database_name)
    }

    // Helper function to connect without a specific database (useful for creating the DB)
    pub fn without_db(&self) -> PgConnectOptions {
        PgConnectOptions::from_str(&format!(
            "postgres://{}:{}@{}:{}/{}",
            self.username, self.password, self.host, self.port, self.database_name
        ))
        .expect("Failed to parse database URL")
    }
}

#[derive(Debug, Deserialize)]
pub struct ApplicationConfig {
    pub host: String,
    pub port: u16,
}

impl Config {
    pub fn from_env() -> Result<Self, config::ConfigError> {
        let s = ExternalConfig::builder() // Use the aliased name
            // Start by merging in the default values
            .set_default("application.host", "127.0.0.1")?
            .set_default("application.port", 8081)?
            // Add in settings from the .env file (for local development)
            // .env file is ignored in .gitignore
            .add_source(File::with_name(".env").required(false))
            // Add in settings from environment variables (with a prefix of APP)
            // E.g. `APP_DATABASE_HOST=localhost` would set `database.host`
            .add_source(
                Environment::with_prefix("APP")
                    .prefix_separator("_")
                    .separator("__"),
            )
            .build()?;

        s.try_deserialize()
    }
}
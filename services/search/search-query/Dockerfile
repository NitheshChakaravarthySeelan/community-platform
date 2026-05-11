FROM rust:latest as builder

# Install system dependencies
RUN apt-get update && apt-get install -y protobuf-compiler pkg-config libssl-dev && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy shared proto definitions
COPY shared/proto ./shared/proto

# Copy the service's Cargo.toml and Cargo.lock
COPY services/search/search-query/Cargo.toml ./services/search/search-query/Cargo.toml
COPY services/search/search-query/Cargo.lock ./services/search/search-query/Cargo.lock

# Copy the service's build script and sources
COPY services/search/search-query/build.rs ./services/search/search-query/build.rs
COPY services/search/search-query/src ./services/search/search-query/src

# Build the final binary
WORKDIR /app/services/search/search-query
RUN cargo build --release

FROM debian:bookworm-slim
RUN apt-get update && apt-get install -y ca-certificates && rm -rf /var/lib/apt/lists/*

# Copy the compiled binary from the builder stage
COPY --from=builder /app/services/search/search-query/target/release/search-query /usr/local/bin/search-query

# Command to run the application
CMD ["/usr/local/bin/search-query"]

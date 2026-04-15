FROM rust:latest as builder

# Install system dependencies
RUN apt-get update && apt-get install -y protobuf-compiler pkg-config libssl-dev && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy shared proto definitions
COPY shared/proto ./shared/proto

# Copy the service's Cargo.toml and Cargo.lock
COPY services/search/search-index/Cargo.toml ./services/search/search-index/Cargo.toml
COPY services/search/search-index/Cargo.lock ./services/search/search-index/Cargo.lock

# Copy the service's build script and sources
COPY services/search/search-index/build.rs ./services/search/search-index/build.rs
COPY services/search/search-index/src ./services/search/search-index/src

# Build dependencies (this layer will be cached)
# WORKDIR /app/services/search/search-index
# RUN mkdir src && echo "fn main() {}" > src/main.rs && cargo build --release
# RUN rm -rf target/release/.fingerprint target/release/.cargo-lock target/release/deps src

# Build the final binary
WORKDIR /app/services/search/search-index
RUN cargo build --release

FROM debian:bookworm-slim
RUN apt-get update && apt-get install -y ca-certificates && rm -rf /var/lib/apt/lists/*

# Copy the compiled binary from the builder stage
COPY --from=builder /app/services/search/search-index/target/release/search-index /usr/local/bin/search-index

# Command to run the application
CMD ["/usr/local/bin/search-index"]

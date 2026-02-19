# ---------- Stage 1: Build ----------
FROM rust:bookworm AS builder

# Accept DATABASE_URL as a build argument
ARG DATABASE_URL
ENV DATABASE_URL=${DATABASE_URL}
# We will run sqlx prepare online during the build
ENV SQLX_OFFLINE=true

# Install system dependencies
RUN apt-get update && apt-get install -y protobuf-compiler libsasl2-dev pkg-config ca-certificates && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy pnpm-workspace.yaml to the root for workspace detection by build.rs
COPY pnpm-workspace.yaml /pnpm-workspace.yaml

# --- Copy manifests first (better caching) ---
COPY services/catalog/product-lookup-rust/Cargo.toml ./services/catalog/product-lookup-rust/Cargo.toml
COPY services/catalog/product-lookup-rust/Cargo.lock ./services/catalog/product-lookup-rust/Cargo.lock

# --- Copy build script and sources ---
COPY services/catalog/product-lookup-rust/build.rs ./services/catalog/product-lookup-rust/build.rs
COPY services/catalog/product-lookup-rust/src ./services/catalog/product-lookup-rust/src

# --- Copy shared proto definitions ---
COPY shared/proto ./shared/proto

# Copy generated SQLx metadata for offline mode
COPY services/catalog/product-lookup-rust/.sqlx ./.sqlx



# --- Build ---
WORKDIR /app/services/catalog/product-lookup-rust
RUN cargo build --release --bin product-lookup-rust


# ---------- Stage 2: Runtime ----------
FROM debian:bookworm-slim

# Install runtime dependencies
RUN apt-get update && apt-get install -y ca-certificates libsasl2-2 && rm -rf /var/lib/apt/lists/*

# Copy the compiled binary
COPY --from=builder /app/services/catalog/product-lookup-rust/target/release/product-lookup-rust /usr/local/bin/product-lookup-rust

# Run it
CMD ["/usr/local/bin/product-lookup-rust"]

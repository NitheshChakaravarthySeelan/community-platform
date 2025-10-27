FROM rust:1.78-slim-bookworm as builder

WORKDIR /app

# Copy Cargo.toml and Cargo.lock
COPY Cargo.toml Cargo.lock ./

# Build dependencies (this layer will be cached)
RUN mkdir src && echo "fn main() {}" > src/main.rs && cargo build --release
RUN rm -rf target/release/.fingerprint target/release/.cargo-lock target/release/deps src

# Copy source code and build final binary
COPY . .
RUN cargo build --release

FROM debian:bookworm-slim

# Copy the compiled binary from the builder stage
COPY --from=builder /app/target/release/search-query /usr/local/bin/search-query

# Command to run the application
CMD ["/usr/local/bin/search-query"]
# Use a Rust base image
FROM rust:latest as builder

# Set the working directory
WORKDIR /app

# Copy source code
COPY . .

# Build the Rust application
RUN cargo build --release

# Start a new stage from a smaller base image
FROM debian:stretch-slim

# Copy the built binary from the builder stage
COPY --from=builder /app/target/release/inventory-service /usr/local/bin/inventory-service

# Expose the application port
EXPOSE 8080

# Run the application
CMD ["inventory-service"]

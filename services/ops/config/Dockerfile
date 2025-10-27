FROM golang:1.22-slim-bookworm as builder

WORKDIR /app

# Copy go.mod and go.sum
COPY go.mod go.sum ./

# Download dependencies
RUN go mod download

# Copy source code
COPY . .

# Build the application
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o config .

FROM debian:bookworm-slim

# Copy the compiled binary from the builder stage
COPY --from=builder /app/config /usr/local/bin/config

# Command to run the application
CMD ["/usr/local/bin/config"]
FROM golang:1.22-slim-bookworm as builder

WORKDIR /app

# Copy go.mod and go.sum
COPY go.mod go.sum ./

# Download dependencies
RUN go mod download

# Copy source code
COPY . .

# Build the application
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o metrics .

FROM debian:bookworm-slim

# Copy the compiled binary from the builder stage
COPY --from=builder /app/metrics /usr/local/bin/metrics

# Command to run the application
CMD ["/usr/local/bin/metrics"]
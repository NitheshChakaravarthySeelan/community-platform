#!/bin/bash

# CheckoutX Native Startup Script
# Starts all services in the background. 
# Prerequisites: Ensure 'make infra-up' is running for Postgres/Kafka/Redis.

LOG_DIR=".logs"
mkdir -p $LOG_DIR

echo "🚀 Starting CheckoutX Services Natively..."

# Helper function to start a service
# Usage: start_service <dir> <command> <name>
start_service() {
    local dir=$1
    local cmd=$2
    local name=$3

    echo "  -> Starting $name..."
    cd $dir
    nohup $cmd > "../../$LOG_DIR/$name.log" 2>&1 &
    # Store PID for later shutdown
    echo $! > "../../$LOG_DIR/$name.pid"
    cd - > /dev/null
}

# 1. Gateway BFF (Node.js)
start_service "apps/gateway-bff" "pnpm run dev" "gateway-bff"

# 2. Checkout UI (Node.js)
start_service "checkout-ui" "pnpm run dev" "checkout-ui"

# 3. Cart CRUD (Node.js)
start_service "services/cart/cart-crud" "pnpm run dev" "cart-crud"

# 4. Checkout Orchestrator (Python)
start_service "services/checkout/checkout-orchestrator" "poetry run uvicorn checkout_orchestrator.main:app --port 8000" "checkout-orchestrator"

# 5. Inventory Write (Rust)
start_service "services/inventory/inventory-write" "cargo run" "inventory-write"

# 6. Inventory Read (Rust)
start_service "services/inventory/inventory-read" "cargo run" "inventory-read"

# 7. Auth Service (Java)
start_service "services/users/auth-service" "./mvnw spring-boot:run" "auth-service"

# 8. User Service (Java)
start_service "services/users/user-service" "./mvnw spring-boot:run" "user-service"

# 9. Product Read (Java)
start_service "services/catalog/product-read" "./mvnw spring-boot:run" "product-read"

# 10. Product Write (Java)
start_service "services/catalog/product-write" "./mvnw spring-boot:run" "product-write"

# 11. Payment Gateway (Java)
start_service "services/orders/payment-gateway" "./mvnw spring-boot:run" "payment-gateway"

# 12. Order Create (Java)
start_service "services/orders/order-create" "./mvnw spring-boot:run" "order-create"

echo ""
echo "✅ All services started in the background."
echo "📝 Logs are available in the '$LOG_DIR' directory."
echo "🛑 To stop all services, run: pkill -F .logs/*.pid (or just pkill -f 'java|node|python|cargo')"

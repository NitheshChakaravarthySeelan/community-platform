# Community Platform

This repository contains the foundational microservices and frontend applications for the Community Platform project. It's designed as a polyglot, event-driven microservices architecture leveraging a saga pattern for distributed transactions.

## Overview

The platform uses a Backend-For-Frontend (BFF) to expose its public API, coordinating with various backend microservices. Key architectural principles include:

*   **Microservices:** Independent services for specific business capabilities.
*   **Event-Driven Architecture (EDA):** Kafka for asynchronous communication and event streaming.
*   **Saga Pattern:** Orchestration-based sagas (coordinated by `checkout-orchestrator`) for distributed transactions.
*   **Polyglot Development:** Services written in TypeScript, Python, Java, and Rust.

## Prerequisites

Ensure you have the following installed on your system:

*   **Docker & Docker Compose:** For running core infrastructure (Postgres, Redis, Kafka).
*   **Java Development Kit (JDK) 17+:** For Java-based microservices.
*   **Maven:** For building and running Java services.
*   **Node.js 20+:** For TypeScript/Next.js services.
*   **pnpm:** A fast, disk space efficient package manager for Node.js projects (used in this monorepo).
*   **Python 3.12+:** For Python-based microservices.
*   **Poetry:** Python package and dependency manager (used for Python services).
*   **Rust Toolchain:** For Rust-based microservices.

## Local Development

Follow these steps to get the entire platform running on your local machine.

### 1. Start Core Infrastructure

The shared infrastructure (Postgres, Redis, Kafka) is managed with Docker Compose.

```sh
docker-compose -f infra/docker/docker-compose.dev.yml up -d
```

You can check the status of the containers with `docker ps`.

### 2. Run Backend Microservices

Each microservice needs to be started individually, typically in a separate terminal.

#### Java Services

Run each in its respective directory:

*   **`payment-gateway`:**
    ```sh
    cd services/orders/payment-gateway
    ./mvnw spring-boot:run
    ```
*   **`order-create`:**
    ```sh
    cd services/orders/order-create
    ./mvnw spring-boot:run
    ```
*   **`product-write`:**
    ```sh
    cd services/catalog/product-write
    ./mvnw spring-boot:run
    ```
*   **`product-read`:**
    ```sh
    cd services/catalog/product-read
    ./mvnw spring-boot:run
    ```

#### Python Services

*   **`checkout-orchestrator`:**
    ```sh
    cd services/checkout/checkout-orchestrator
    poetry install
    poetry run uvicorn checkout_orchestrator.main:app --reload --port 8000
    ```

#### TypeScript Services

*   **`cart-crud`:**
    ```sh
    cd services/cart/cart-crud
    pnpm install
    pnpm start
    ```

#### Rust Services

*   **`inventory-write`:**
    ```sh
    cd services/inventory/inventory-write
    cargo run
    ```

### 3. Run Frontend Application (Gateway-BFF)

The Next.js Backend-For-Frontend application:

```sh
cd apps/gateway-bff
pnpm install
pnpm dev
```

Once running, the Gateway-BFF will be available at [http://localhost:3000](http://localhost:3000).

### 4. Trigger the Checkout Saga (Example)

With all services running, you can initiate a checkout process by sending a POST request to the `gateway-bff`. This will trigger the distributed saga coordinated by the `checkout-orchestrator`.

```sh
curl -X POST http://localhost:3000/api/checkout/initiate \
-H "Content-Type: application/json" \
-d '{
  "userId": "d7a4b1f0-9c2d-4e3f-8a1b-0c5e7f8a9b0c",
  "items": [
    { "productId": "prod-abc-1", "quantity": 2 },
    { "productId": "prod-xyz-2", "quantity": 1 }
  ],
  "totalAmount": 100.50
}'
```

Expected Response (Happy Path):
```json
{
  "message": "Checkout initiated successfully",
  "orderId": "...",
  "status": "PROCESSING"
}
```

### Documentation

For a deeper understanding of the project, please refer to the comprehensive documentation:

*   **[Architecture Overview](docs/architecture.md)**: A high-level view of the system's structure and principles.
*   **[Failure Scenarios and Resilience](docs/failure-scenarios.md)**: Details on how the system handles failures, especially within distributed sagas.
*   **[Architectural Tradeoffs](docs/tradeoffs.md)**: Discussion of the design choices made and their implications.

### Contributing

We welcome contributions to the Community Platform! Please see our [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.
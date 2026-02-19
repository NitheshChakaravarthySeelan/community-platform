# Community Platform

This repository contains the foundational microservices and frontend applications for the Community Platform project. It's designed as a polyglot, event-driven microservices architecture leveraging a saga pattern for distributed transactions.

## Overview

The platform uses a Backend-For-Frontend (BFF) to expose its public API, coordinating with various backend microservices. Key architectural principles include:

- **Microservices:** Independent services for specific business capabilities.
- **Event-Driven Architecture (EDA):** Kafka for asynchronous communication and event streaming.
- **Saga Pattern:** Orchestration-based sagas (coordinated by `checkout-orchestrator`) for distributed transactions.
- **Polyglot Development:** Services written in TypeScript, Python, Java, and Rust.

## Prerequisites

Ensure you have the following installed on your system:

- **Docker & Docker Compose:** For running core infrastructure (Postgres, Redis, Kafka).
- **Java Development Kit (JDK) 17+:** For Java-based microservices.
- **Maven:** For building and running Java services.
- **Node.js 20+:** For TypeScript/Next.js services.
- **pnpm:** A fast, disk space efficient package manager for Node.js projects (used in this monorepo).
- **Python 3.12+:** For Python-based microservices.
- **Poetry:** Python package and dependency manager (used for Python services).
- **Rust Toolchain:** For Rust-based microservices.

## Local Development

Follow these steps to get the entire platform running on your local machine.

### 1. Start the Full Containerized Stack (Recommended)

The most straightforward way to run the entire platform is using the provided Docker Compose configuration. This will build and start all microservices and core infrastructure.

```sh
docker compose -f infra/docker/docker-compose.dev.yml up --build -d
```

You can monitor the logs for all services using:

```sh
docker compose -f infra/docker/docker-compose.dev.yml logs -f
```

### 2. Service Endpoints & Port Mappings

Once the stack is running, you can access the various services at the following local endpoints:

| Service                   | Host Port | Internal Port | Description                  |
| :------------------------ | :-------- | :------------ | :--------------------------- |
| **Gateway BFF**           | `3004`    | `3000`        | Public Next.js API & UI      |
| **Cart CRUD**             | `3001`    | `3000`        | Cart management service      |
| **Auth Service**          | `3002`    | `3002`        | User authentication (Java)   |
| **Product Read**          | `8082`    | `8082`        | Catalog read service (Java)  |
| **Product Write**         | `8081`    | `8081`        | Catalog write service (Java) |
| **Inventory Write**       | `8088`    | `8080`        | Inventory updates (Rust)     |
| **Inventory Read**        | `50052`   | `50052`       | Inventory read (Rust/gRPC)   |
| **Checkout Orchestrator** | `8000`    | `8000`        | Saga coordinator (Python)    |
| **Postgres**              | `5432`    | `5432`        | Main relational database     |
| **Kafka**                 | `9092`    | `9092`        | Event streaming broker       |
| **MinIO UI**              | `9001`    | `9001`        | Object storage dashboard     |
| **MinIO API**             | `9005`    | `9002`        | Object storage API           |
| **Prometheus**            | `9090`    | `9090`        | Metrics collection           |
| **Grafana**               | `3003`    | `3000`        | Metrics visualization        |

### 3. Individual Service Development (Alternative)

If you prefer to run specific services natively for development while keeping core infrastructure in Docker:

#### A. Start Core Infrastructure only

```sh
# Stop everything first if running
docker compose -f infra/docker/docker-compose.dev.yml down
# Start only the base infrastructure
docker compose -f infra/docker/docker-compose.dev.yml up -d postgres redis kafka zookeeper
```

#### B. Run Backend Microservices Natively

Each microservice can be started in its respective directory:

- **Java (e.g., Auth):** `cd services/users/auth-service && ./mvnw spring-boot:run`
- **Python (Checkout):** `cd services/checkout/checkout-orchestrator && poetry run uvicorn ...`
- **Node.js (Cart):** `cd services/cart/cart-crud && pnpm start`
- **Rust (Inventory):** `cd services/inventory/inventory-write && cargo run`

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

- **[Architecture Overview](docs/architecture.md)**: A high-level view of the system's structure and principles.
- **[Failure Scenarios and Resilience](docs/failure-scenarios.md)**: Details on how the system handles failures, especially within distributed sagas.
- **[Architectural Tradeoffs](docs/tradeoffs.md)**: Discussion of the design choices made and their implications.

### Contributing

We welcome contributions to the Community Platform! Please see our [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

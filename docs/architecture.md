# System Architecture

This document provides a comprehensive overview of the community platform's microservices architecture, outlining its core components, communication patterns, and key technological choices.

## 1. High-Level Architecture

The platform operates as a distributed system, composed of multiple independent microservices. The primary interaction pattern involves external clients communicating with a Backend-For-Frontend (BFF), which then orchestrates or delegates tasks to various internal microservices, often leveraging an event-driven approach.

```text
+---------------------+      +---------------------+      +---------------------+
|                     |      |                     |      |                     |
|    External Client  |<---->|     Gateway-BFF     |<---->|    Microservice A   |
| (Web/Mobile App)    |      | (apps/gateway-bff)  |      |   (e.g., Product)   |
|   (HTTP/JSON)       |      |    (HTTP/JSON)      |      |     (Kafka/Events)  |
+---------------------+      +---------------------+      +---------------------+
                                       ^    ^
                                       |    | (Kafka Events/Commands)
                                       |    |
                                       v    v
                               +---------------------+      +---------------------+
                               |                     |      |                     |
                               |  Checkout Orchestrator |<-->|    Microservice B   |
                               | (Python FastAPI)    |      | (e.g., Inventory,   |
                               |  (Kafka Consumer/   |      |  Payment, Order, Cart)|
                               |    Producer)        |      |    (Kafka Reactive) |
                               +---------------------+      +---------------------+
                                       ^
                                       | (Saga State Persistence)
                                       v
                               +---------------------+
                               |                     |
                               |  PostgreSQL Database|
                               |  (Saga State)       |
                               +---------------------+
```

## 2. Core Architectural Principles

*   **Microservices:** The system is composed of small, independent, and loosely coupled services, each responsible for a specific business capability. This enables independent development, deployment, and scaling.
*   **Event-Driven Architecture (EDA):** Kafka serves as the central nervous system, enabling asynchronous communication, decoupling services, and facilitating complex workflows like distributed sagas.
*   **Orchestration-based Saga Pattern:** Complex business transactions spanning multiple services (e.g., Checkout) are managed by a dedicated orchestrator service (`checkout-orchestrator`), which maintains the saga state and coordinates steps via Kafka events and commands.
*   **Backend-For-Frontend (BFF):** A specialized aggregation layer (`gateway-bff`) caters to the needs of specific client applications, providing a tailored API and often initiating event-driven flows directly.
*   **Polyglot Persistence:** Each microservice can choose the most suitable database technology for its specific data storage needs (e.g., PostgreSQL for relational data, as seen in `checkout-orchestrator`).
*   **Polyglot Programming:** Services are implemented in various languages (e.g., TypeScript for BFF, Python for Orchestrator, Java for Payment/Order, Rust for Inventory), allowing teams to select the best tool for the job.

## 3. Key Components and Their Roles

*   **External Clients:** Web or mobile applications that interact with the platform. They communicate exclusively with the `gateway-bff` via HTTP/JSON.
*   **Gateway-BFF (`apps/gateway-bff`):**
    *   **Technology:** Next.js (TypeScript) utilizing the App Router.
    *   **Role:** The public API gateway. It exposes tailored HTTP/JSON APIs to external clients. For complex workflows like checkout, it acts as the initial event producer, publishing events directly to Kafka. For simpler requests, it might proxy HTTP calls to other microservices.
*   **Kafka:**
    *   **Role:** The central asynchronous message broker. It facilitates event streaming, command distribution, and event sourcing. It's critical for decoupling services and coordinating distributed transactions (sagas).
    *   **Topics:** Various topics are used for specific events and commands (e.g., `checkout.checkout-events`, `checkout.inventory-command`).
*   **Checkout Orchestrator (`services/checkout/checkout-orchestrator`):**
    *   **Technology:** Python FastAPI.
    *   **Role:** The coordinator for the checkout saga. It consumes events from Kafka, maintains the state of ongoing sagas in its PostgreSQL database, and publishes commands (via Kafka) to participating services to drive the saga forward or initiate compensation.
*   **Participating Microservices (e.g., Payment, Inventory, Order, Cart):**
    *   **Technologies:** Java (e.g., `payment-gateway`, `order-create`), Rust (e.g., `inventory-write`), TypeScript (e.g., `cart-crud`).
    *   **Role:** Implement specific business capabilities. Within the checkout saga, they act as "headless" services: they consume commands from Kafka, perform their designated task, and publish events back to Kafka to inform the orchestrator of their status. They typically manage their own dedicated data stores.
*   **Protobuf (`shared/proto` & `scripts/generate_proto.sh`):**
    *   **Role:** Defines language-agnostic data contracts (messages) and service interfaces for gRPC. The `generate_proto.sh` script compiles these definitions into strongly-typed code for various languages (TypeScript, Python, Go, Java, Rust). While currently JSON is used for Kafka payloads, the intention is to transition to Protobuf for type safety and efficiency.
*   **PostgreSQL Databases:**
    *   **Role:** Each microservice typically has its own PostgreSQL database (or other suitable datastore) for persistence, adhering to the principle of independent data ownership. The `checkout-orchestrator` specifically uses PostgreSQL to persist the state of ongoing sagas.

## 4. Communication Patterns

*   **External Client ↔ Gateway-BFF:** Synchronous HTTP/JSON requests and responses.
*   **Gateway-BFF → Kafka:** Asynchronous event publishing (JSON payloads, intended for Protobuf). This is used to initiate sagas.
*   **Checkout Orchestrator ↔ Kafka:** Asynchronous event consumption and command/event publishing (JSON payloads, intended for Protobuf). This coordinates the saga.
*   **Participating Microservices ↔ Kafka:** Asynchronous command consumption and event publishing (JSON payloads, intended for Protobuf). This allows them to perform their tasks and report status.
*   **BFF ↔ Microservices (HTTP Proxying):** For certain requests (not saga initiation), the BFF may proxy HTTP/JSON requests to the HTTP endpoints of specific microservices.
*   **Internal Service APIs:** While not extensively identified as gRPC servers in Java/Python services, the presence of `.proto` files indicates the architectural intent or future capability for gRPC-based synchronous inter-service communication.

## 5. Data Flow Example: Checkout Initiation Saga

The checkout initiation provides a clear example of the system's data flow:

1.  **Client Request:** An external client sends a `POST /api/checkout/initiate` HTTP/JSON request to the `gateway-bff`.
2.  **BFF Initiates Event:** The `gateway-bff`'s `src/app/api/checkout/initiate/route.ts` handler generates a `saga_id` and publishes a `CheckoutInitiatedEvent` (as a JSON payload) to the `checkout.checkout-events` Kafka topic.
3.  **Orchestrator Consumes:** The `checkout-orchestrator`'s `KafkaConsumerManager` consumes this `CheckoutInitiatedEvent`.
4.  **Orchestrator Updates State:** It updates the saga's state to `INVENTORY_RESERVATION_PENDING` in its PostgreSQL database.
5.  **Orchestrator Commands Inventory:** It then publishes a `ReserveInventory` command (JSON payload) to the `checkout.inventory-command` Kafka topic. This command includes a `reply_to_topic` pointing back to `checkout.checkout-events`.
6.  **Inventory Service Reacts:** The `inventory-write` service consumes the `ReserveInventory` command, attempts to reserve items, and publishes an `InventoryReservedEvent` (or `InventoryReservationFailedEvent`) back to the `checkout.checkout-events` topic.
7.  **Saga Continues:** The `checkout-orchestrator` consumes this event, updates its saga state, and publishes the next command in the sequence (e.g., `ProcessPayment` to the `payment-gateway` via `checkout.payment-command`), repeating the pattern until the saga is completed or compensated.

This illustrates the highly decoupled and asynchronous nature of the system's internal operations.
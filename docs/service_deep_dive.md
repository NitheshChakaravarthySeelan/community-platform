# CheckoutX Service Deep Dive Memory

This document serves as a persistent technical reference for all services within the CheckoutX ecosystem. It is updated dynamically as new insights are gathered.

## Global Architectural Patterns

- **Inter-service Communication**: gRPC (Synchronous), Kafka (Asynchronous/Saga).
- **Data Consistency**: Saga Pattern (Orchestrated), CQRS (Catalog).
- **Identity**: JWT-based, passed via gRPC metadata (`x-user-id`, etc.).
- **Resilience**: Circuit Breakers (Python/Java), Idempotency keys in Sagas.

---

## 1. apps/gateway-bff (Node.js/Next.js)

- **Role**: Entry point for `checkout-ui`. Translates HTTP to internal gRPC/Kafka.
- **Key Files**:
  - `src/app/api/checkout/initiate/route.ts`: Entry point for checkout saga.
  - `src/lib/grpc/`: gRPC client wrappers.
  - `src/lib/kafka.ts`: Kafka event publisher.
- **Communication**:
  - Inbound: HTTP/JSON.
  - Outbound: gRPC (to Cart), Kafka (`checkout.checkout-initiated`).

## 2. services/checkout/checkout-orchestrator (Python/FastAPI)

- **Role**: The Saga "Conductor". Manages the state machine for checkouts.
- **Tech Stack**: FastAPI, SQLAlchemy (PostgreSQL), `aiokafka`, `pybreaker`.
- **State Management**: `saga_states` table in Postgres tracks `context`, `state`, and `processed_event_ids`.
- **Logic**: Listens for `checkout.checkout-initiated` and dispatches commands to Payment, Inventory, and Order services.

## 3. services/cart/cart-crud (Node.js/TypeScript)

- **Role**: Core cart management.
- **Communication**: gRPC Server (`src/grpc/cart.service.ts`).
- **Data**: Likely PostgreSQL (needs confirmation).

## 4. services/catalog/product-write (Java/Spring Boot)

- **Role**: Authoritative source for product data.
- **Pattern**: CQRS Command side.
- **Findings**: `UpdateProductHandler` currently fails to publish `ProductUpdatedEvent` to Kafka, creating a potential sync issue with the read model.

## 5. services/catalog/product-read (Java/Spring Boot)

- **Role**: Read-optimized view of the product catalog.
- **Pattern**: CQRS Query side.
- **Communication**: gRPC Server.

## 6. services/catalog/product-lookup-rust (Rust)

- **Role**: High-performance, low-latency product lookups.
- **Tech Stack**: `tonic` (gRPC), `sqlx` (Postgres).

## 7. Commerce Services (Java / Spring Boot)

- **Role**: Handles business-heavy domains like Cart Pricing, Snapshots, Brands, and Categories.
- **Tech Stack**: Spring Boot 3.2, Spring Data JPA, Hibernate.
- **Data Persistence**: PostgreSQL (Production), H2 (Development).
- **Communication**: gRPC Servers for internal logic, Kafka for event consumption (e.g., `CheckoutInitiatedEvent`).

## 8. Order & Financial Services (Java / Spring Boot)

- **Services**: `order-create`, `order-read`, `payment-gateway`, `invoice`, `refund`, `wallet`.
- **Role**: Orchestrates the order lifecycle and financial transactions.
- **Communication**: Heavily integrated with Kafka for asynchronous processing (e.g., `invoice` generates events after `order-create` completes).
- **Persistence**: PostgreSQL.

## 9. Pricing Engines (Java / Spring Boot)

- **Services**: `discount-engine`, `list-price`, `tax-calculation`.
- **Role**: Calculates dynamic pricing, applied discounts, and region-specific taxes.
- **Communication**: Synchronous gRPC servers invoked by the `checkout-orchestrator` or `cart-pricing`.

## 10. Infrastructure & Notification Services (Go)

- **Services**: `email-service`, `push-service`, `sms-service`.
- **Role**: Dispatches multi-channel alerts to users.
- **Tech Stack**: Go (for small footprint and high concurrency).
- **Communication**: Kafka Consumers (listening for `order.placed`, `payment.failed`, etc.) + gRPC Servers for direct triggers.

## 11. Operational & Observability Services (Go)

- **Services**: `config`, `log-forwarder`, `metrics`, `tracing`.
- **Role**: Standardizes environment configuration and system-wide observability.
- **Tech Stack**: Go, OpenTelemetry (OTLP), Prometheus.

## 12. Inventory Management (Rust)

- **Services**: `inventory-read`, `inventory-write`.
- **Role**: Manages stock levels with high concurrency and strong consistency.
- **Tech Stack**: Rust (Actix-web, `sqlx`).
- **Communication**: `inventory-write` acts as a Kafka producer for stock changes; `inventory-read` provides gRPC/HTTP lookups.
- **Persistence**: PostgreSQL.

## 13. Search & Recommendation Services (Rust / Python)

- **Search (Rust)**: `search-index` and `search-query` use Meilisearch for ultra-fast full-text search.
- **AI Agent (Python/FastAPI)**: `agent-service` uses LangGraph and Google Gemini Pro for autonomous shopping orchestration via gRPC.
- **Sync Logic (Python/FastAPI)**: `rec-model-service` and `warehouse-sync` for model serving and external inventory synchronization.

## 14. User & Auth Services (Java / Spring Boot)

- **Services**: `auth-service`, `user-service`.
- **Role**: Manages user profiles, credentials, and JWT issuance.
- **Communication**: gRPC and HTTP/REST.
- **Persistence**: PostgreSQL.

## Contract & Serialization Audit (Protobuf vs. Custom)

| Service                   | gRPC Strategy       | Kafka Strategy          | Serialization      |
| :------------------------ | :------------------ | :---------------------- | :----------------- |
| **gateway-bff**           | **Shared Protobuf** | **Custom TS Interface** | Mixed (Proto/JSON) |
| **checkout-orchestrator** | **Generated PB2**   | **Custom Python Dict**  | Mixed (Proto/JSON) |
| **product-write**         | N/A                 | **Custom Java POJO**    | **JSON**           |
| **product-read**          | **Shared Protobuf** | N/A                     | **Protobuf**       |
| **inventory-write**       | **Tonic/Prost**     | **Custom Rust Struct**  | Mixed (Proto/JSON) |
| **search-index**          | N/A                 | **Custom Rust Struct**  | **JSON**           |

### **The "Contract Gap" Analysis**

A critical inconsistency exists across the platform: **gRPC is standardized on Protobuf, while Kafka is fragmented across custom JSON implementations.**

1.  **Risk**: If `product-write` (Java) updates a field in its custom `ProductUpdatedEvent` POJO, the `search-index` (Rust) or `checkout-orchestrator` (Python) will fail to deserialize the message silently or crash, as they lack a shared schema.
2.  **Performance**: JSON serialization/deserialization is significantly slower and produces larger payloads than Protobuf's binary format.
3.  **Recommendation**: All Kafka events should be migrated to use the `shared/proto/*.proto` definitions to ensure a single, language-agnostic source of truth.

---

_(End of Detailed Service Map)_

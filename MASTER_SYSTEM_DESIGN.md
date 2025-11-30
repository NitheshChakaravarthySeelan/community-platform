# Master System Design: Project Chimera - The Enterprise E-Commerce Blueprint

**Version:** 5.1 (Definitive Master Blueprint with A/B Testing & Feature Flags)
**Status:** DRAFT
**Author:** Gemini Architect

---

## Part I: The Vision & Guiding Philosophy

### Chapter 1: Executive Summary & Core Mission

#### 1.1. Project Vision

Project Chimera is conceived as a cloud-native, hyper-scalable, and deeply resilient **composable commerce** platform, engineered to the highest enterprise standards. This document is the master blueprint for its creation. It is designed to be exhaustive, serving not only as an architectural specification but as a comprehensive learning codex for modern High-Level Design (HLD) and Low-Level Design (LLD). Our goal is to build a system capable of supporting millions of concurrent users, processing thousands of transactions per second, and delivering a rich, real-time, AI-driven user experience.

#### 1.2. Business & Technical Imperatives

- **Business Imperatives:**
  - **Velocity:** Achieve rapid, independent feature development and deployment through extreme modularity.
  - **Experience:** Deliver sub-second user-perceived latency and a seamless, personalized journey.
  - **Reliability:** Guarantee 99.99% availability for all critical-path services.
  - **Intelligence:** Unlock deep business insights and drive revenue through a real-time, data-first approach.
- **Technical Imperatives:**
  - **Decoupling:** A fully decoupled microservices architecture where services can be developed, deployed, and scaled independently.
  - **Resilience:** An "asynchronous-first" communication model that is inherently resilient to partial system failures.
  - **Observability:** Achieve "glass box" visibility with comprehensive, correlated logs, metrics, and traces for every transaction.
  - **Security:** A "zero-trust" security model is to be implemented at every layer.
  - **Automation:** A fully automated infrastructure and GitOps-driven deployment pipeline.

### Chapter 2: The Architectural Tenets (Non-Negotiable Principles)

This design is governed by a set of foundational principles. Adherence to these tenets is mandatory for ensuring the long-term success and scalability of the platform.

1.  **Domain-Driven Design (DDD):** The system is decomposed into microservices aligned with business domains (Bounded Contexts). Each service has its own ubiquitous language and clear boundaries.
2.  **Database per Service:** Each microservice has exclusive, private ownership of its data via a dedicated database. There will be **no** direct database-to-database communication or shared tables between services. This is the most critical principle for true microservice independence.
3.  **Asynchronous-First Communication:** Inter-service communication defaults to asynchronous eventing. This decouples services, builds a fault-tolerant system, and allows for temporal scaling. Synchronous communication is the deliberate exception, not the rule.
4.  **API-First Contracts:** All service interactions are governed by well-defined, versioned contracts. We use **OpenAPI 3.0** for synchronous REST APIs and **Protocol Buffers (Protobuf) 3** for internal gRPC communication.
5.  **Infrastructure as Code (IaC):** All infrastructure is declaratively defined using **Terraform**. No manual changes ("click-ops") are permitted in staging or production environments. This ensures reproducibility, auditability, and disaster recovery.
6.  **Zero Trust Security:** No service implicitly trusts another, regardless of its location on the network. All API calls, internal and external, must be authenticated and authorized. Inter-service communication will be secured using **mutual TLS (mTLS)**.
7.  **Immutability:** Infrastructure components and application containers are treated as immutable. To update, we replace; we do not patch.

### Chapter 2.1: Non-Functional Requirements (NFRs) and Service Level Objectives (SLOs)

Beyond qualitative goals, our system must meet specific, measurable non-functional requirements. These are defined as Service Level Objectives (SLOs) for critical services and flows.

- **Availability:**
  - **Critical APIs (Checkout, Product Details, Login):** Target `99.99%` uptime (max ~5 minutes downtime per month).
  - **Non-Critical APIs (Search, Recommendations):** Target `99.9%` uptime (max ~43 minutes downtime per month).
- **Latency (p99):**
  - **User-facing Read APIs (`product-read`, `getCart`):** `< 150ms`.
  - **User-facing Write APIs (`addItem`, `placeOrder`):** `< 300ms`.
  - **Internal gRPC Calls:** `< 50ms`.
- **Durability:** Data committed to any database must be stored durably with `99.999%` probability.
- **Consistency:** Eventual consistency for read models; strong consistency for write models (e.g., within a service's database).
- **Scalability:** System must handle a 10x surge in traffic within 5 minutes without degradation of SLOs.
- **Recoverability:** Full system recovery from catastrophic failure within `RTO < 4 hours` and `RPO < 15 minutes` (Recovery Time/Point Objective).

---

## Part II: High-Level Design (HLD) & System-Wide Concepts

### Chapter 3: The Macro-Architecture

The system is a distributed network of microservices communicating primarily through an event bus, with a secure API Gateway acting as the single, hardened entry point for all external clients.

```
+--------------------------------------------------------------------------------------+
|                           Clients (Web, Mobile App, Smart Mirror, Voice Assistant)     |
+--------------------------------------------------------------------------------------+
             | (HTTPS: GraphQL/REST)                               ^ (WSS: WebSockets)
             v                                                     |
+--------------------------------------------------------------------------------------+
|                            API Gateway & BFF (gateway-bff)                             |
| [AuthN/AuthZ (OIDC)] [Rate Limit] [GraphQL Endpoint] [WebSocket Mgmt] [Edge Functions] |
| [Feature Flags/A/B Test Engine]                                                      |
+--------------------------------------------------------------------------------------+
   | (gRPC)    | (REST)      | (gRPC)         | (REST)           | (gRPC)           | (File Upload)
   v           v             v                v                  v                  v
+--------+ +---------+ +-----------+ +----------------+ +----------------+ +----------------+
| auth-  | | user-   | | product-  | | cart-crud      | | checkout-orch  | | visual-search- |
| service| | service | | read/write| |                | |                | | service        |
+--------+ +---------+ +-----------+ +----------------+ +----------------+ +----------------+
   | (DB)    | (DB)        | (DB)           | (DB/Cache)       | (DB)           | (Object Store)
   v         v             v                v                  v                v
+--------+ +---------+ +-----------+ +----------------+ +----------------+ +----------------+
|  Auth  | |  Users  | |  Catalog  | | Carts (Redis/  | |  Orders        | | Product Images |
|   DB   | |   DB    | |    DB     | |   Postgres)    | |    DB          | | (e.g., S3)     |
+--------+ +---------+ +-----------+ +----------------+ +----------------+ +----------------+
      ^         ^             ^                                  ^
      | (CDC)   | (CDC)       | (CDC)                            | (CDC)
      +---------+-------------+----------------------------------+
             |
             v
+--------------------------------------------------------------------------------------+
|                        Event Streaming & Messaging Platform                          |
|         [Kafka: Core Events - High Throughput, Replayable Log]                       |
|         [RabbitMQ: Ancillary Tasks - Smart Routing, Retries]                         |
|--------------------------------------------------------------------------------------|
| [Topics: users, products, orders]           [Exchanges: notifications, analytics]    |
+--------------------------------------------------------------------------------------+
      | (Event) | (Event)     | (Event)         | (Event)          | (Event)
      v         v             v                 v                  v
+--------+ +---------+ +-----------+ +----------------+ +----------------+
| search-| | notif-  | | inventory-| | audit-service  | | rec-model-svc  |
| index  | | service | | write     | |                | | (Real-time)    |
+--------+ +---------+ +-----------+ +----------------+ +----------------+
```

### Chapter 4: Deep Dive - Asynchronous Communication with Kafka & RabbitMQ

#### 4.1. The Role of Event-Driven Architecture (EDA)

- **HLD Concept:** EDA is crucial for enterprise systems. It achieves maximum **decoupling** between services, enhances **resilience** (producers don't wait for consumers), and allows for **scalability** (consumers can be added/removed independently). It fundamentally shifts communication from direct requests to reactive processing of emitted facts.

#### 4.2. Kafka: The System's Immutable Log for Core Business Events

- **Concept:** Kafka is a distributed, fault-tolerant, high-throughput streaming platform. It acts as a durable, append-only commit log for events.
- **Why Kafka?**
  - **High Throughput & Low Latency:** Designed to handle millions of messages per second, critical for real-time analytics and fast event propagation.
  - **Durability & Replayability:** Events are stored for a configurable retention period. New services can "replay" past events to build their state, supporting new features without re-processing source data.
  - **Scalability:** Partitions allow parallel consumption, scaling horizontally.
  - **Ordered Delivery:** Within a partition, Kafka guarantees message order, essential for maintaining data consistency for specific entities (e.g., all events for a `userId` go to the same partition).
- **Our Use Cases (HLD/LLD):**
  - **Core Business Events:** `OrderCreated`, `ProductUpdated`, `UserRegistered`, `PaymentProcessed`.
  - **Real-time Data Feeds:** `UserInteractionEvents` (clicks, views), `InventoryLevelChanges`.
  - **LLD Detail - Partitioning Strategy:** For topics like `orders_stream`, the `order_id` or `user_id` will be used as the Kafka message key. This ensures all events related to a specific order/user are processed by the same consumer instance in order, preventing race conditions (e.g., processing `OrderCancelled` before `OrderCreated`).
  - **LLD Detail - Consumer Groups:** Services subscribe using consumer groups. Each message in a topic is delivered to only one consumer instance within each consumer group, while all instances across different groups receive all messages.

#### 4.3. RabbitMQ: The Smart Task Delegator for Ancillary Workloads

- **Concept:** RabbitMQ is a traditional message broker that excels at complex routing and reliable task queuing. It's a "smart broker" that actively pushes messages to consumers, crucial for Day 2 operations.
- **Why RabbitMQ?**
  - **Complex Routing:** Flexible exchanges (direct, topic, fanout, headers) allow for intricate message distribution patterns.
  - **Advanced Queuing Features:** Built-in support for message acknowledgements, retries with exponential backoff, Time-To-Live (TTL) for messages/queues, and Dead-Letter Queues (DLQ) for failed messages. This ensures that tasks are eventually processed.
  - **Push Model:** Actively pushes messages to consumers, reducing consumer polling overhead.
- **Our Use Cases (HLD/LLD):**
  - **Notifications:** Sending welcome emails, order updates via SMS/Email, push notifications (where delivery can be retried).
  - **Background Processing:** Image resizing, report generation, data cleanup tasks.
  - **LLD Detail - Notification Service:** A `notification-service` will consume Kafka events (e.g., `OrderCreated`) and then publish a specialized message to a RabbitMQ Topic Exchange (e.g., with routing key `email.order.confirmation`). An `email-worker` service (Python/Go) will bind to this exchange to consume and process emails. If sending fails, RabbitMQ's DLQ will ensure the message isn't lost.

#### 4.4. Change Data Capture (CDC) with Debezium

- **HLD Concept:** CDC with Debezium is a cutting-edge pattern that solves a fundamental microservice problem: reliably publishing events when data changes in a database.
- **Why CDC?**
  - **Transactional Guarantees:** Events are published to Kafka only after the database transaction commits, ensuring atomicity and preventing the "dual-write" problem (where a DB write succeeds but event publishing fails, leading to inconsistency).
  - **Non-Invasive:** Application code is not burdened with event publishing logic, making services simpler and less error-prone.
  - **Historical Data:** Debezium streams all changes, providing a complete history of the database to Kafka, enabling sophisticated auditing, analytics, and derived views.
- **How it Works (Step-by-Step LLD):**
  1.  **Database Configuration:** Enable logical decoding in PostgreSQL (e.g., `wal_level = logical`, `max_replication_slots`).
  2.  **Debezium Connector:** A Debezium connector (deployed via Kafka Connect service) monitors the `orders`, `products`, and `users` PostgreSQL transaction logs.
  3.  **Event Generation:** For every `INSERT`, `UPDATE`, `DELETE` in the monitored tables, Debezium generates a structured event (e.g., Avro, JSON) containing `before` and `after` states of the row.
  4.  **Kafka Publishing:** These events are automatically published to dedicated Kafka topics (e.g., `dbserver1.public.orders`, `dbserver1.public.products`).
- **Benefit:** This is the most robust way to implement the **Transactional Outbox Pattern** implicitly, ensuring consistency between a service's database state and the events it emits.

### Chapter 5: Deep Dive - Database Scaling & Management

#### 5.1. Database-per-Service Pattern

- **HLD Concept:** This fundamental microservices principle dictates that each service owns and exclusively accesses its own database.
- **Why?**
  - **Decoupling:** Services can evolve their schema, choose their database technology, and scale independently.
  - **Autonomy:** Teams can deploy and manage their data without coordination overhead.
- **Our Implementation:** All core services (`auth`, `users`, `catalog`, `orders`) will use PostgreSQL. `cart-crud` will use both PostgreSQL (for persistence) and Redis (for active carts). `search-index` will use Elasticsearch.

#### 5.2. Read Replicas for Scaling Read-Heavy Workloads

- **HLD Concept:** A technique to scale read operations horizontally.
- **How it Works:**
  1.  **Primary Database:** One PostgreSQL instance handles all `WRITE` operations (`INSERT`, `UPDATE`, `DELETE`) for a service's database.
  2.  **Read Replicas:** Multiple (e.g., 3-5) read-only PostgreSQL instances asynchronously replicate data from the primary.
  3.  **Application Routing (LLD):** The `product-read` service's data access layer (e.g., connection pool or ORM configuration) will explicitly route all write queries to the primary and load-balance all read queries across the replicas.
- **Benefit:** Significantly increases read throughput without impacting write performance of the primary.
- **Trade-off (Replication Lag):** We must accept that there will be a small delay (milliseconds to seconds) for data to appear on the replicas. This is acceptable for eventual consistency models (e.g., product catalog).

#### 5.3. Sharding for Scaling Write-Heavy Workloads

- **HLD Concept:** For services with massive data volume and high write throughput (like `orders` or `users` at extreme scale), a single primary database becomes a bottleneck. Sharding (or horizontal partitioning) splits the data across multiple independent databases.
- **Our Strategy (Entity-Based Sharding - LLD Detail):**
  1.  **Sharding Key:** We will shard the `orders` and `users` databases by `user_id`. This ensures all data related to a single user resides on the same shard.
  2.  **Routing Layer:** A dedicated data access layer (e.g., a simple client library or a sophisticated proxy) within the service (e.g., `orders` service) will compute a hash of the `user_id` to determine which shard to send the query to.
  3.  **Benefit:** Distributes write load and storage capacity across multiple databases, avoiding single-database bottlenecks.
- **Challenge:**
  - **Cross-Shard Queries:** Queries that don't use the sharding key (e.g., "how many orders were placed yesterday across all users?") become complex and expensive, often requiring scatter/gather queries across all shards.
  - **Resharding:** Rebalancing data when adding new shards is a complex, high-impact operation. This emphasizes the importance of choosing a good sharding key early.

### Chapter 6: Deep Dive - Caching Strategies with Redis

- **HLD Concept:** Caching is crucial for performance and scalability, reducing latency and database load. Redis is a versatile in-memory data store for this.

#### 6.1. Cache-Aside Pattern (LLD Detail for `product-read`)

- **Concept:** The application code is responsible for managing interactions with the cache.
- **How it Works (`product-read` service):**

  ```typescript
  // Example pseudocode for ProductService.getProductById
  async getProduct(productId: string): Promise<Product | null> {
    // LLD Step 1: Check Redis cache first
    let product = await redisClient.get(`product:${productId}`);
    if (product) {
      // Cache Hit: Return cached data
      return JSON.parse(product);
    }

    // LLD Step 2: Cache Miss - fetch from primary DB (PostgreSQL)
    product = await productRepository.findById(productId);
    if (product) {
      // LLD Step 3: Populate cache with a TTL
      await redisClient.setEx(`product:${productId}`, JSON.stringify(product), TTL_SECONDS);
    }
    return product;
  }
  ```

- **Benefits:** Simple to implement, application has full control over caching logic.
- **Trade-offs:** "Stale data" possible if DB is updated before cache TTL expires.

#### 6.2. Write-Through Pattern (LLD Detail for `auth-service` / Session Management)

- **Concept:** Data is written to the cache and the database simultaneously. The application considers the write complete only after both operations succeed.
- **How it Works (`auth-service` for session/token data):**
  ```typescript
  // Example pseudocode for AuthService.saveSessionToken
  async saveSession(token: string, userId: string, expiresAt: Date): Promise<void> {
    const sessionData = { userId, expiresAt };
    // LLD Step 1: Write to Redis (cache)
    await redisClient.set(`session:${token}`, JSON.stringify(sessionData), (expiresAt.getTime() - Date.now()) / 1000);
    // LLD Step 2: Write to persistent store (e.g., a session DB table)
    await sessionRepository.save(token, userId, expiresAt);
  }
  ```
- **Benefits:** Cache always consistent with database for writes.
- **Trade-offs:** Higher write latency due to dual-write.

### Chapter 7: Capacity Planning & Cost Estimation Methodology

#### 7.1. Methodology

- **Traffic Profile:** Start with assumptions about user base, activity, and peak-to-average ratios.
- **Service Breakdown:** Estimate QPS/writes per service.
- **Resource Sizing:** Translate QPS/writes to CPU, Memory, Disk I/O.
- **Cost Models:** Apply cloud provider pricing models (e.g., AWS EC2, RDS, ElastiCache, Kafka, EKS).
- **Iteration:** This is an iterative process, refined with actual load testing.

#### 7.2. Example: `product-read` Service (Initial Estimate)

- **Assumptions:**
  - 1 million Daily Active Users (DAU).
  - Each user views 20 product pages/day on average.
  - Peak-to-average traffic ratio: 10x.
  - Cache Hit Ratio: 90% (after warm-up).
- **Calculations:**
  - `Total daily product views = 1,000,000 users * 20 views/user = 20,000,000 views/day`
  - `Average QPS (Reads) = 20,000,000 views / 86,400 seconds/day = ~231 QPS`
  - `Peak QPS (Reads) = 231 QPS * 10 = ~2,310 QPS`
  - `Peak Cache Miss QPS = 2,310 QPS * (1 - 0.90) = ~231 QPS (to PostgreSQL)`
  - `Peak Redis QPS = 2,310 QPS (cache reads) + 231 QPS (cache writes) = ~2,541 QPS`
- **Resource Sizing (Illustrative):**
  - `product-read` (JVM): 2 instances of `t3.medium` (2 vCPU, 4GB RAM) to handle ~1000 QPS each. Total 4 vCPU, 8GB RAM.
  - `Redis`: 1 node of `cache.t3.medium` (2 vCPU, 3.8GB RAM).
  - `PostgreSQL (Read Replica):` `db.t3.small` for 231 QPS. Need to consider primary DB for writes.
- **Estimated Monthly Cost (Illustrative - AWS us-east-1):**
  - `EC2 instances: $70`
  - `ElastiCache (Redis): $80`
  - `RDS (PostgreSQL, primary only for now): $100`
  - `Total (minimum): ~$250/month` (excluding other services, network, etc.)

---

### Chapter 8: Data Governance & Schema Evolution Strategy

#### 8.1. Data Governance Principles

- **Ownership:** Each microservice is the sole owner and authority for its data.
- **Contracts:** Data shared via Kafka events or gRPC must adhere to strict schemas.
- **Quality:** Data quality is a shared responsibility, enforced by validation layers.

#### 8.2. Schema Evolution for Kafka Events (Avro & Schema Registry)

- **Concept:** As our services evolve, their data models will change. We need a robust way to manage schema changes without breaking downstream consumers.
- **How it Works:**
  1.  All Kafka events will be serialized using **Avro**. Avro schemas are inherently versioned and support schema evolution rules.
  2.  A **Schema Registry** (e.g., Confluent Schema Registry) will store and manage all Avro schemas. Producers register their schemas, and consumers retrieve them, allowing dynamic schema resolution.
- **Evolution Rules:**
  - **Backward Compatibility:** New versions of a schema can be read by code generated from an older schema. (e.g., adding an optional field).
  - **Forward Compatibility:** Older code can read data written with a newer schema. (e.g., removing an optional field).
  - We will enforce **Backward Compatibility** to prevent older consumers from breaking.

#### 8.3. Schema Evolution for gRPC (Protocol Buffers)

- **Concept:** Similar to Kafka, gRPC contracts (Protobuf) also need careful evolution.
- **Rules:**
  - Do not change the field number of any existing field.
  - Do not change the data type of an existing field.
  - New fields can be added, but they must be `optional` or have `default` values.
  - Fields can be deprecated but should never be removed.

---

### Chapter 9: Security Threat Modeling (Using STRIDE)

Security is paramount. We will apply the STRIDE threat model to identify and mitigate risks.

#### 9.1. STRIDE Model Overview

- **S**poofing: Impersonating someone or something else.
- **T**ampering: Modifying data.
- **R**epudiation: Denying an action.
- **I**nformation Disclosure: Exposing sensitive data.
- **D**enial of Service (DoS): Making a system unavailable.
- **E**levation of Privilege: Gaining unauthorized access.

#### 9.2. Application of STRIDE to Project Chimera (Illustrative Examples)

- **Threat:** **Spoofing** (Client impersonates another user)
  - **Mitigation:** `gateway-bff` performs **JWT validation** (signature, expiration, issuer). `auth-service` uses strong authentication (2FA optional).
- **Threat:** **Spoofing** (Malicious service impersonates `payment-gateway`)
  - **Mitigation:** **mTLS** for all inter-service communication. Each service verifies the certificate of the service it's communicating with.
- **Threat:** **Tampering** (User modifies cart price during checkout)
  - **Mitigation:** The `checkout-orchestrator` must **never trust client-side price data**. It must re-fetch and validate prices from `product-read` before payment processing. All data mutations (e.g., quantity update) are done server-side.
- **Threat:** **Information Disclosure** (Sensitive user data exposed)
  - **Mitigation:** **Encryption in Transit** (TLS for all HTTP/gRPC, Kafka's SSL/SASL). **Encryption at Rest** for sensitive database fields. Strict **access controls (RBAC/ABAC)** on data access. Data minimization.
- **Threat:** **Denial of Service (DoS)** (API Gateway overwhelmed)
  - **Mitigation:** **Rate Limiting** at the `gateway-bff`. Auto-scaling for `gateway-bff` instances. **Circuit Breakers** and **Bulkheads** to prevent cascading failures.
- **Threat:** **Elevation of Privilege** (Unauthorized access to admin features)
  - **Mitigation:** **Role-Based Access Control (RBAC)** implemented in each service (e.g., only `admin` role can call `product-write`'s update endpoint).

---

## Part III: Low-Level Design (LLD) & Implementation Plan

### Phase 0: The Foundation - Security, Headless Gateway & User Service

- **Objective:** Establish a secure, headless-ready foundation.
- **Services to Build:** `user-service`, `auth-service`, `gateway-bff`.
- **`user-service` (Java/Spring Boot) LLD:**
  - **Pattern:** Standard 3-tier architecture (Controller, Service, Repository).
  - **Repository:** Use Spring Data JPA's `JpaRepository` for boilerplate CRUD operations.
  - **Database:** PostgreSQL. Schema as defined in Part IV.
  - **API:** Simple REST API for CRUD operations on user profiles (`/users/{id}`).
- **`auth-service` (Node.js/Express) LLD:**
  - **Pattern:** Use Passport.js with a custom `passport-local` strategy for username/password validation. Use `jsonwebtoken` for signing/verifying JWTs.
  - **Configuration:** JWT secret keys, token expiration times, and issuer details will be loaded from environment variables.
- **`gateway-bff` (Node.js/Express) LLD:**
  - **Pattern:** Use Express Middleware for cross-cutting concerns.
  - **Middleware 1 (Authentication):** A middleware on all protected routes will extract the JWT from the `Authorization` header, verify its signature using the `auth-service`'s public key, and attach the decoded user payload to the `request` object.
  - **Modern Feature (GraphQL):** Implement an `/graphql` endpoint using `apollo-server-express`. Create GraphQL resolvers that make downstream calls to other services (e.g., a `user` query that calls the `user-service`).

### Phase 0.4: Experimentation & Release Management (A/B Testing & Feature Flags)

- **Objective:** Enable data-driven decision making and safe, rapid feature rollouts.
- **HLD Concept:** Decoupling deployments from releases. Using traffic splitting to test variations.
- **LLD Design (`gateway-bff`):**
  - The `gateway-bff` will integrate with a feature flag management system (e.g., LaunchDarkly client, or an internal service backed by Redis).
  - **Client-Side Flags:** For UI changes, the `gateway-bff` can inject feature flag states into the frontend application.
  - **Server-Side Flags:** For API changes or backend logic, the `gateway-bff` can route traffic to different service versions or modify API responses based on active flags for a given user or request.
  - **A/B Testing:** The `gateway-bff` can split a percentage of user traffic (e.g., 50% to "Variant A", 50% to "Variant B") to test different features or UI designs.
- **Implementation Steps (`gateway-bff`):**
  1.  Integrate a feature flag client library.
  2.  Define sample feature flags (e.g., `new-homepage-layout`, `new-checkout-flow`).
  3.  Implement logic in `gateway-bff` to evaluate flags and conditionally render/route.

### Phase 1: Core Commerce Flow & RESTful Baseline

- **Objective:** Get a functional end-to-end "add to cart" flow.
- **Services to Build/Modify:** `product-read/write`, `cart-crud`, `checkout-orchestrator`.

#### 1.1. `product-write` (Java/Spring Boot)

- **HLD Role:** Handles all create, update, delete operations for product data.
- **LLD Design:**
  - **Pattern (CQRS Write Side):** Exposes `POST`, `PUT`, `DELETE` REST APIs.
  - **Database:** Owns the `catalog` PostgreSQL database.
- **Implementation Steps:**
  1.  Create `services/catalog/product-write/` directory.
  2.  Define `Product` JPA entity with fields like `id`, `su`, `name`, `description`, `base_price_cents`, `image_urls`.
  3.  Implement `ProductWriteRepository` and `ProductWriteService`.
  4.  Implement `ProductWriteController` for `POST /products`, `PUT /products/{id}`, `DELETE /products/{id}`.

#### 1.2. `product-read` (Java/Spring Boot)

- **HLD Role:** Provides highly performant read access to product data.
- **LLD Design:**
  - **Pattern (CQRS Read Side):** Exposes `GET` REST APIs for products. This service is optimized for reads and will eventually incorporate caching and read replicas.
  - **Database:** Shares the `catalog` PostgreSQL database (read-only access).
- **Implementation Steps:**
  1.  Create `services/catalog/product-read/` directory.
  2.  Implement `ProductReadRepository` and `ProductReadService`.
  3.  Implement `ProductReadController` for `GET /products`, `GET /products/{id}`.

#### 1.3. `cart-crud` (Node.js/Express)

- **HLD Role:** Manages user shopping carts.
- **LLD Design:**
  - **Pattern (Adapter - LLD):** Create a `ProductServiceAdapter` class to encapsulate HTTP calls to `product-read`. This hides the communication details from the core `CartService`.
  - **Dependency Injection:** `CartService` will receive the `ProductServiceAdapter` through DI.
- **Implementation Steps:**
  1.  Modify `services/cart/cart-crud/src/services/cart.service.ts`.
  2.  Create `src/adapters/ProductServiceAdapter.ts` to make HTTP calls to `product-read` (e.g., using `axios`).
  3.  Inject this adapter into `CartService` and use it to fetch real product details when adding items or retrieving the cart.

#### 1.4. `checkout-orchestrator` (Java/Spring Boot)

- **HLD Role:** Coordinates the synchronous steps of the checkout process.
- **LLD Design:**
  - **Pattern (Orchestration - HLD):** This service makes direct HTTP calls to `cart-crud`, `payment-gateway` (mock for now), and then writes the final order to its own database.
  - **Compensating Transactions (HLD consideration):** If payment fails but the cart is cleared, a compensating action is needed (e.g., restore cart).
- **Implementation Steps:**
  1.  Create `services/checkout/checkout-orchestrator/` directory.
  2.  Implement `CheckoutService` making `axios` calls to `cart-crud` and mock `payment-gateway`.
  3.  Define `Order` JPA entity.
  4.  Implement `OrderRepository` and `OrderController` for `POST /checkout`.

### Phase 2: High-Performance Caching with Redis

- **Objective:** Significantly improve read performance for `product-read`.
- **Infrastructure Task:** Add Redis service to `infra/docker/docker-compose.dev.yml`.
- **Service to Modify:** `product-read`.

#### 2.1. `product-read` (Java/Spring Boot) - Caching Implementation

- **HLD Role:** Provide lightning-fast product lookups.
- **LLD Design:**
  - **Pattern (Decorator - LLD):** The `ProductReadService` will be decorated with caching logic.
  - **How (LLD Detail):**
    1.  Create `ProductReadRepository` (interface).
    2.  Implement `PostgresProductReadRepository` (actual DB access).
    3.  Implement `CachingProductReadRepository` that also implements `ProductReadRepository`.
    4.  `CachingProductReadRepository`'s methods (`findById`, `findAll`) will implement the **Cache-Aside** strategy.
    5.  In Spring's configuration, `ProductReadService` will be injected with `CachingProductReadRepository`, which internally holds an instance of `PostgresProductReadRepository`.
- **Implementation Steps:**
  1.  Add `redis.clients:jedis` dependency to `product-read/pom.xml`.
  2.  Create `src/config/RedisConfig.java` to configure Jedis client.
  3.  Modify `src/service/ProductReadService.java` to implement Cache-Aside logic using `RedisTemplate` or direct `Jedis` calls.
  4.  Set appropriate TTLs (e.g., 5-10 minutes) for product cache entries.

### Phase 3: Transition to Event-Driven with CDC & Kafka

- **Objective:** Decouple services and build a resilient system backbone.
- **Infrastructure Task:** Add Zookeeper, Kafka, and Debezium/Kafka Connect to `infra/docker/docker-compose.dev.yml`.
- **Configuration Task:** Configure a Debezium PostgreSQL connector to monitor the `orders` and `products` databases in the `checkout-orchestrator` and `product-write` services respectively.

#### 3.1. `product-write` & `checkout-orchestrator` (Event Production via CDC)

- **HLD Role:** Sources of truth for product and order events.
- **LLD Design:** No direct application code changes are needed for event production in these services. The magic happens externally.
- **Implementation Steps:**
  1.  Ensure `wal_level = logical` is set in your PostgreSQL configurations.
  2.  Write Terraform/Docker Compose scripts to deploy Debezium Kafka Connect.
  3.  Configure Debezium connectors for `checkout-orchestrator`'s `orders` DB and `product-write`'s `catalog` DB.

#### 3.2. `inventory-service` (Java/Spring Boot)

- **HLD Role:** Manages product stock levels.
- **LLD Design:**
  - **Pattern (Kafka Consumer - LLD):** Implements a Kafka consumer to subscribe to `dbserver1.public.orders` topic.
  - **Idempotency (LLD):** Since Kafka consumers can reprocess messages, the inventory decrement logic must be idempotent. This can be achieved by tracking processed `order_item_id`s in a local database or using transactional outbox in its own DB.
- **Implementation Steps:**
  1.  Create `services/inventory/inventory-write/` directory.
  2.  Define `Inventory` JPA entity.
  3.  Implement Kafka consumer to listen to `orders_stream`.
  4.  Logic to decrement stock for each item in the order.

#### 3.3. `notification-service` (Python/FastAPI)

- **HLD Role:** Sends various notifications (email, SMS).
- **LLD Design:**
  - **Pattern (Kafka Consumer + RabbitMQ Producer - LLD):** Consumes events from Kafka, then publishes specific tasks to RabbitMQ.
  - **RabbitMQ Integration:** Uses a `RabbitMQPublisher` (Adapter Pattern) to publish messages to a Topic Exchange.
- **Implementation Steps:**
  1.  Create `services/notifications/email-service/` directory (renamed from `email-service` for clarity to `notification-service` to encompass email/SMS/push).
  2.  Implement a Kafka consumer for `users_stream` (for welcome emails) and `orders_stream` (for order confirmations).
  3.  Implement logic to construct email/SMS content.
  4.  Publish email/SMS jobs to RabbitMQ.

### Phase 4: Full-Text Search & Analytics

- **Objective:** Implement powerful, user-friendly search.
- **Infrastructure Task:** Add Elasticsearch service to `infra/docker/docker-compose.dev.yml`.
- **Service to Build:** `search-index`.

#### 4.1. `search-index` (Python/FastAPI)

- **HLD Role:** Creates and maintains a fast, searchable index of product data.
- **LLD Design:**
  - **Pattern (Kafka Consumer + Elasticsearch Client - LLD):** Consumes events from `dbserver1.public.products`.
  - **Data Transformation:** Transforms CDC events (which contain `before` and `after` states) into documents suitable for Elasticsearch.
  - **API:** Exposes a simple REST API (e.g., `GET /search?q={query}`) for `gateway-bff`.
- **Implementation Steps:**
  1.  Create `services/search/search-index/` directory.
  2.  Implement Kafka consumer to listen to `products_stream`.
  3.  Use `elasticsearch-py` client to index products.
  4.  Implement FastAPI endpoints for searching.

#### 4.2. `gateway-bff` (Modified for Search)

- **LLD Design:**
  - **Routing:** The `/search` endpoint will now proxy requests to the `search-index` service.
- **Implementation Steps:**
  1.  Update `gateway-bff` routing configuration.

### Phase 5: Optimize with gRPC & Go Real-Time

- **Objective:** Improve internal performance and UX.
- **Shared Task:** In `shared/proto`, create a `pricing.proto` file defining the `DiscountService`.

#### 5.1. `discount-engine` (Go)

- **HLD Role:** Provides fast, internal discount calculations.
- **LLD Design:**
  - **Pattern (gRPC Server - LLD):** Implements the `DiscountService` as a gRPC server.
  - **Technology:** Go's strong concurrency features are ideal for a high-performance service.
- **Implementation Steps:**
  1.  Create `services/pricing/discount-engine/` directory.
  2.  Implement gRPC server logic for `GetApplicableDiscounts`.
  3.  Integrate a simple rule engine (e.g., hardcoded rules or a Go library) to apply discounts.

#### 5.2. `cart-pricing` (Java/Spring Boot)

- **HLD Role:** Calculates the total price of a cart, including discounts.
- **LLD Design:**
  - **Pattern (gRPC Client - LLD):** Makes gRPC calls to the `discount-engine`.
  - **Adapter:** Create a `DiscountServiceGrpcAdapter` to encapsulate gRPC client logic.
- **Implementation Steps:**
  1.  Create `services/pricing/cart-pricing/` directory.
  2.  Use gRPC-Java libraries to generate client stubs from `pricing.proto`.
  3.  Implement `CartPricingService` making gRPC calls.

#### 5.3. `gateway-bff` (Modified for Real-Time)

- **HLD Role:** Manages WebSocket connections for live updates.
- **LLD Design:**
  - **Pattern (WebSocket Server - LLD):** Uses a library like `ws` or `socket.io` to manage persistent client connections.
  - **Pub/Sub (LLD):** Uses Redis Pub/Sub for internal communication to push events to relevant WebSocket clients.
- **Implementation Steps:**
  1.  Implement `/ws` WebSocket endpoint.
  2.  Set up Redis client for Pub/Sub.
  3.  Create a background worker that subscribes to Kafka topics (e.g., `orders_status_updates`) and publishes relevant messages to Redis Pub/Sub channels (e.g., `user_updates:{userId}`).
  4.  WebSocket handler listens to Redis Pub/Sub and forwards messages to connected clients.

### Phase 6: Production Hardening

- **Objective:** Prepare for enterprise scale.
- **Cross-Cutting Task:** Integrate OpenTelemetry SDKs into the `main` function or entry point of every single service.
- **`cart-crud` (Client-side Resilience) LLD:**
  - **Pattern (Circuit Breaker):** Use a library like `opossum` for Node.js. Wrap the `ProductServiceAdapter`'s HTTP call in a circuit breaker. Configure it to "open" (fail fast) after 5 consecutive failures and stay open for 30 seconds.
- **Infrastructure Task (Terraform):** In `infra/terraform`, create modules to define an EKS/GKE cluster, a managed PostgreSQL service (RDS/Cloud SQL) with read replicas enabled, and a managed Redis service (ElastiCache/MemoryStore).

---

## Part IV: Detailed Data Models & Schemas

(This section continues for hundreds of lines, detailing every single table, column, data type, and constraint for every service's database as described in the LLD sections above.)

### 4.1. `auth-service` (PostgreSQL)

```sql
-- Table: credentials
-- Stores user authentication credentials. user_id references users.id in user-service.
-- Primary key user_id for direct lookup
CREATE TABLE IF NOT EXISTS credentials (
    user_id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL, -- Ensure unique emails
    password_hash VARCHAR(255) NOT NULL, -- Stored as bcrypt hash for security
    failed_login_attempts INTEGER DEFAULT 0, -- For brute-force protection
    last_login_at TIMESTAMPTZ,
    account_locked BOOLEAN DEFAULT FALSE, -- For account lockout
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Table: refresh_tokens
-- Stores refresh tokens for long-lived sessions, enabling token revocation.
-- token_hash is indexed for quick lookup
CREATE TABLE IF NOT EXISTS refresh_tokens (
    token_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Unique identifier for the token
    user_id UUID NOT NULL REFERENCES credentials(user_id) ON DELETE CASCADE,
    token_hash VARCHAR(255) UNIQUE NOT NULL, -- Hashed refresh token for security
    expires_at TIMESTAMPTZ NOT NULL,
    issued_at TIMESTAMPTZ DEFAULT NOW(),
    revoked BOOLEAN DEFAULT FALSE, -- Allows explicit revocation
    CHECK (expires_at > issued_at) -- Ensure token has a valid lifespan
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
```

### 4.2. `user-service` (PostgreSQL)

```sql
-- Table: users
-- Core user profile information.
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY, -- Corresponds to user_id in credentials from auth-service
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    preferred_currency VARCHAR(3) DEFAULT 'USD',
    marketing_opt_in BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Table: user_addresses
-- Stores multiple addresses for a user (shipping, billing).
CREATE TABLE IF NOT EXISTS user_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE, -- User owns this address
    address_line_1 VARCHAR(255) NOT NULL,
    address_line_2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state_province VARCHAR(100),
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    address_type VARCHAR(50) NOT NULL, -- e.g., 'SHIPPING', 'BILLING', 'HOME', 'WORK'
    is_default BOOLEAN DEFAULT FALSE, -- One default address per type
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id, address_type, is_default) WHERE is_default = TRUE -- Enforce one default per type
);

CREATE INDEX IF NOT EXISTS idx_user_addresses_user_id ON user_addresses (user_id);
CREATE INDEX IF NOT EXISTS idx_user_addresses_user_id_type ON user_addresses (user_id, address_type);
```

### 4.3. `product-read`/`product-write` (Catalog PostgreSQL)

```sql
-- Table: categories
-- Defines product categories (e.g., Electronics, Clothing).
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    parent_id UUID REFERENCES categories(id) ON DELETE SET NULL, -- For hierarchical categories
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Table: brands
-- Defines product brands.
CREATE TABLE IF NOT EXISTS brands (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Table: products
-- Stores core product information.
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku VARCHAR(100) UNIQUE NOT NULL, -- Stock Keeping Unit
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL, -- Foreign key to a categories table (not detailed here)
    brand_id UUID REFERENCES brands(id) ON DELETE SET NULL, -- Foreign key to a brands table (not detailed here)
    base_price_cents INTEGER NOT NULL CHECK (base_price_cents >= 0), -- Stored in cents to avoid floating point issues
    currency VARCHAR(3) DEFAULT 'USD',
    image_urls TEXT[], -- Array of image URLs (S3 links)
    available_stock INTEGER DEFAULT 0 CHECK (available_stock >= 0), -- Reflects latest stock from inventory-service
    is_active BOOLEAN DEFAULT TRUE, -- Whether product is visible/purchasable
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_products_sku ON products (sku);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products (category_id);
CREATE INDEX IF NOT EXISTS idx_products_brand_id ON products (brand_id);
CREATE INDEX IF NOT EXISTS idx_products_name ON products USING GIN (to_tsvector('english', name)); -- For basic search in DB

-- Table: product_variants (Optional: for products with size, color, etc.)
-- Stores variations of a product.
CREATE TABLE IF NOT EXISTS product_variants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_sku VARCHAR(100) UNIQUE NOT NULL,
    attributes JSONB, -- e.g., {"color": "red", "size": "M", "material": "cotton"}
    price_cents INTEGER CHECK (price_cents >= 0), -- Can override base_price_cents
    stock_level INTEGER DEFAULT 0 CHECK (stock_level >= 0),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_product_variants_product_id ON product_variants (product_id);
```

### 4.4. `cart-crud` (Carts PostgreSQL & Redis)

```sql
-- Table: carts (PostgreSQL)
-- Persistent storage for user carts. Primarily for recovery and long-term storage of inactive carts.
CREATE TABLE IF NOT EXISTS carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL, -- Ensures one active cart per user
    items JSONB NOT NULL DEFAULT '[]'::jsonb, -- Array of cart items (productId, quantity, priceAtTime)
    last_accessed_at TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_carts_user_id ON carts (user_id);
CREATE INDEX IF NOT EXISTS idx_carts_last_accessed_at ON carts (last_accessed_at);
```

- **Redis Structure for Active Carts (LLD):**
  - Key: `cart:{userId}` (e.g., `cart:a1b2c3d4-e5f6-7890-1234-567890abcdef`)
  - Type: Redis Hash
  - Fields: `productId:quantity` (e.g., `product-123:2`, `product-456:1`)
  - TTL: Configured for active session (e.g., 2 hours). If cart expires, `cart-crud` can try to load from Postgres.
- **Redis Structure for Enriched Carts (LLD):**
  - Key: `enriched_cart:{userId}`
  - Type: Redis String (JSON blob)
  - Value: Full JSON representation of the cart after fetching product details, calculating totals, etc., for quick retrieval by `gateway-bff`.

### 4.5. `orders` Service (PostgreSQL)

```sql
-- Table: orders
-- Stores placed orders. Source of truth for all order details.
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    billing_address_id UUID NOT NULL, -- We'll copy address details to avoid FK dependency on user-service at time of order
    shipping_address_id UUID NOT NULL,
    items JSONB NOT NULL, -- Snapshot of items at time of order (productId, quantity, name, priceAtTime)
    subtotal_cents INTEGER NOT NULL CHECK (subtotal_cents >= 0),
    shipping_cents INTEGER DEFAULT 0 CHECK (shipping_cents >= 0),
    tax_cents INTEGER DEFAULT 0 CHECK (tax_cents >= 0),
    discount_cents INTEGER DEFAULT 0 CHECK (discount_cents >= 0),
    total_cents INTEGER NOT NULL CHECK (total_cents >= 0),
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(50) NOT NULL, -- e.g., 'PENDING_PAYMENT', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'
    payment_method_details JSONB, -- Snapshot of payment method at time of order
    payment_transaction_id UUID, -- Reference to payment gateway transaction
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders (user_id);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders (created_at);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders (status);

-- Table: order_events (for internal service audit/state tracking)
-- Stores a history of state changes for an order within the service.
CREATE TABLE IF NOT EXISTS order_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    event_type VARCHAR(100) NOT NULL, -- e.g., 'ORDER_CREATED_INTERNAL', 'PAYMENT_RECEIVED_INTERNAL', 'STOCK_ALLOCATED_INTERNAL'
    event_data JSONB, -- Additional details for the event
    occurred_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_order_events_order_id ON order_events (order_id);
```

---

## Part V: Detailed API Contracts

(This section will grow to contain OpenAPI 3.0 specifications for all REST APIs, Protobuf schemas for gRPC services, and the full GraphQL schema. For brevity in this document, we provide illustrative examples only.)

### 5.1. REST API (OpenAPI 3.0 Snippet for `user-service`)

```yaml
# user-service-api.yaml (Excerpt)
openapi: 3.0.0
info:
  title: User Service API
  description: API for managing user profiles and addresses.
  version: 1.0.0
servers:
  - url: /api/v1/users
tags:
  - name: Users
    description: User profile management
paths:
  /{userId}:
    get:
      summary: Retrieve a user by ID
      operationId: getUserById
      tags:
        - Users
      parameters:
        - in: path
          name: userId
          schema:
            type: string
            format: uuid
          required: true
          description: UUID of the user to retrieve
      responses:
        "200":
          description: User profile found
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/UserProfile"
        "404":
          description: User not found
        "401":
          description: Unauthorized
        "403":
          description: Forbidden
    put:
      summary: Update a user's profile
      operationId: updateUserProfile
      tags:
        - Users
      parameters:
        - in: path
          name: userId
          schema:
            type: string
            format: uuid
          required: true
          description: UUID of the user to update
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/UserProfileUpdate"
      responses:
        "200":
          description: User profile updated successfully
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/UserProfile"
        "400":
          description: Invalid input
        "404":
          description: User not found
        "401":
          description: Unauthorized
        "403":
          description: Forbidden
components:
  schemas:
    UserProfile:
      type: object
      properties:
        id:
          type: string
          format: uuid
          readOnly: true
        firstName:
          type: string
          example: John
        lastName:
          type: string
          example: Doe
        email:
          type: string
          format: email
          readOnly: true # Email managed by auth-service
        phoneNumber:
          type: string
          example: "+15551234567"
        preferredCurrency:
          type: string
          example: "USD"
        createdAt:
          type: string
          format: date-time
          readOnly: true
        updatedAt:
          type: string
          format: date-time
          readOnly: true
      required:
        - firstName
        - lastName
    UserProfileUpdate:
      type: object
      properties:
        firstName:
          type: string
          example: Jane
        lastName:
          type: string
          example: Smith
        phoneNumber:
          type: string
          example: "+15559876543"
        preferredCurrency:
          type: string
          example: "EUR"
```

### 5.2. gRPC API (Protobuf Snippet for `discount-engine`)

```protobuf
// pricing.proto
syntax = "proto3";

package pricing;

option go_package = "github.com/project-chimera/pricing"; // For Go generated code

// DiscountService defines the gRPC service for applying discounts.
service DiscountService {
  // GetApplicableDiscounts returns a list of discounts applicable to a given set of items.
  rpc GetApplicableDiscounts (GetApplicableDiscountsRequest) returns (GetApplicableDiscountsResponse);
}

// DiscountItem represents a single item that can receive a discount.
message DiscountItem {
  string product_id = 1; // Unique identifier for the product/variant
  int32 quantity = 2;    // Number of units
  int32 price_cents = 3; // Price per unit in cents at the time of calculation
}

// GetApplicableDiscountsRequest contains the details needed to request discounts.
message GetApplicableDiscountsRequest {
  string user_id = 1; // Optional: for personalized discounts.
  repeated DiscountItem items = 2; // List of items in the cart.
  string currency = 3; // Currency for the calculation, e.g., "USD".
}

// Discount represents a single discount applied.
message Discount {
  string discount_id = 1; // Unique identifier for the discount rule applied
  string name = 2;        // Human-readable name of the discount, e.g., "Summer Sale"
  int32 amount_cents = 3; // The discount amount in cents (negative for discount, positive for surcharge)
  string applied_to_product_id = 4; // Optional: if discount applies to a specific product
}

// GetApplicableDiscountsResponse contains the list of applied discounts and the total discounted amount.
message GetApplicableDiscountsResponse {
  repeated Discount discounts = 1; // List of individual discounts applied
  int32 total_discount_cents = 2;  // Sum of all discount amounts
}
```

### 5.3. GraphQL Schema (Excerpt from `gateway-bff` Resolver)

```graphql
# typeDefs.graphql (Excerpt)
schema {
  query: Query
  mutation: Mutation
  subscription: Subscription # For real-time updates
}

type Query {
  me: User! # Get current authenticated user profile
  user(id: ID!): User # Admin query
  product(id: ID!): Product # Get single product details
  searchProducts(query: String!, categoryId: ID, brandId: ID, minPrice: Int, maxPrice: Int): [Product!]! # Search with filters
  cart: Cart # Get user's active cart
  order(id: ID!): Order # Get specific order
  orders(userId: ID!): [Order!]! # Get all orders for a user
  # ... more queries for other domains
}

type Mutation {
  updateProfile(input: UserProfileUpdateInput!): User!
  addCartItem(productId: ID!, quantity: Int!): Cart!
  updateCartItem(productId: ID!, quantity: Int!): Cart!
  removeCartItem(productId: ID!): Cart!
  placeOrder(input: PlaceOrderInput!): Order!
  # ... more mutations for actions
}

type Subscription {
  onOrderStatusUpdate(orderId: ID!): Order! # Real-time order status updates
  onCartUpdate: Cart! # Real-time cart changes (e.g., another device updates cart)
}

# Object Types
type User {
  id: ID!
  firstName: String!
  lastName: String!
  email: String!
  phoneNumber: String
  preferredCurrency: String
  addresses: [Address!]!
  createdAt: String!
  updatedAt: String!
  orders: [Order!]!
}

type Address {
  id: ID!
  addressLine1: String!
  addressLine2: String
  city: String!
  stateProvince: String
  postalCode: String!
  country: String!
  addressType: String!
  isDefault: Boolean!
}

type Product {
  id: ID!
  sku: String!
  name: String!
  description: String
  category: Category
  brand: Brand
  basePrice: Int! # in cents
  currency: String!
  imageUrls: [String!]!
  availableStock: Int!
  isActive: Boolean!
  variants: [ProductVariant!]
}

type CartItem {
  productId: ID!
  quantity: Int!
  name: String # Enriched data
  price: Int! # Enriched data
  imageUrl: String # Enriched data
  totalPrice: Int! # Enriched data
}

type Cart {
  id: ID!

  userId: ID!
  items: [CartItem!]!
  totalItems: Int!
  subtotal: Int! # in cents
  totalPrice: Int! # in cents after discounts
  lastAccessedAt: String!
}

type Order {
  id: ID!
  userId: ID!
  billingAddress: Address!
  shippingAddress: Address!
  items: [OrderItem!]!
  subtotal: Int!
  shipping: Int!
  tax: Int!
  discount: Int!
  total: Int!
  currency: String!
  status: String!
  createdAt: String!
  updatedAt: String!
}

type OrderItem {
  productId: ID!
  quantity: Int!
  name: String!
}

---

## Part VI: Infrastructure & Deployment (CI/CD & GitOps)

### 6.1. Infrastructure as Code (IaC) with Terraform
- **Concept:** All cloud resources will be provisioned, managed, and updated using Terraform. This includes:
    -   Kubernetes clusters (EKS/GKE)
    -   Managed PostgreSQL instances (AWS RDS / GCP Cloud SQL) with read replicas
    -   Managed Redis instances (AWS ElastiCache / GCP MemoryStore)
    -   Managed Kafka (AWS MSK / GCP Confluent Cloud) or self-hosted Kafka clusters
    -   Load balancers, networking (VPCs, subnets, security groups)
    -   Object Storage (AWS S3 / GCP Cloud Storage) for product images and backups
- **Repository Structure:** `infra/terraform` will contain logical modules (e.g., `infra/terraform/modules/vpc`, `infra/terraform/modules/eks`).
- **Environments:** Separate Terraform workspaces/configurations for `dev`, `staging`, and `production`.

### 6.2. CI/CD Pipeline (GitHub Actions & ArgoCD)
- **Concept:** A robust Continuous Integration/Continuous Delivery pipeline will automate the build, test, and deployment process. We will adopt a **GitOps** model for deployments.
- **Continuous Integration (CI) - GitHub Actions:**
    -   **Triggers:** `push` events to any branch, `pull_request` events.
    -   **Steps:**
        1.  **Code Linting & Formatting:** Ensure code quality and consistency (e.g., `pnpm lint`, `pnpm format`).
        2.  **Unit Tests:** Run all service-specific unit tests (e.g., `jest`, `JUnit`, `pytest`).
        3.  **Integration Tests:** Run integration tests that mock external dependencies or use test containers for databases.
        4.  **Contract Testing:** Use tools like Pact to verify API contracts between services.
        5.  **Security Scans:** Static Application Security Testing (SAST) using tools like SonarQube or Snyk; dependency scanning.
        6.  **Container Build:** Build Docker images for each service, tag them with commit SHA/version, and push to a Container Registry.
-   **Continuous Delivery (CD) - GitOps with ArgoCD:**
    -   **Deployment Strategy:** The `infra/kubernetes` directory will contain all Kubernetes manifests (Deployments, Services, Ingress, HPA, etc.) for all services.
    -   **ArgoCD:** An instance of ArgoCD running in the Kubernetes cluster will continuously monitor this Git repository. Any changes to the Kubernetes manifests (e.g., updating a Docker image tag) will automatically trigger a deployment to the cluster.
    -   **Deployment Types:** Implement **Rolling Updates** by default. For critical services, explore **Canary Deployments** (gradually roll out new versions to a small subset of users) or **Blue/Green Deployments** (deploy new version alongside old, then switch traffic).

---

This document represents the ultimate blueprint for Project Chimera, providing an unprecedented level of detail for both design and implementation. It will serve as your guiding star throughout the development journey.
```

# Software Engineering Design Document: CheckoutX

**Date:** December 24, 2025
**Author:** Nithesh Chakaravarthy Seelan K
**Version:** 2.0 (Production Survivability Argument)

---

## 1. Problem & Customer Pain

### 1.1. Executive Summary

This document proposes a critical set of architectural and operational enhancements to the Community Platform's core microservices. The primary objective is to transform the system from a functional architecture to a **business-critical, production-survivable distributed system.** We are addressing fundamental risks related to customer trust, financial integrity, operational overhead, and scalability, particularly in the checkout and product discovery domains. The proposed design focuses on guaranteeing key system invariants, ensuring data consistency under extreme conditions, proving scalability through quantitative analysis, and defining robust failure recovery and operational strategies.

### 1.2. The Core Business Risk: Erosion of Customer Trust & Financial Integrity

The most significant and immediate risk to our business lies in vulnerabilities within our critical transaction paths, directly impacting customer trust and financial health. Customers demand secure, accurate, and reliable transactions. Failures in these areas lead to:

- **Direct Customer Financial Loss/Inconvenience:** Incorrect charges, order failures, data inconsistencies leading to disputes and chargebacks.
- **Brand Reputation Damage:** Viral negative feedback, loss of market share.
- **Operational Burden:** Significant manual reconciliation, customer support overload, and fraud investigation costs.

**Current Identified Risks:**

1.  **Client-Side Checkout Data Tampering (Fraud Risk):**
    - **Problem:** The `gateway-bff`'s `POST /api/checkout/initiate` endpoint currently accepts mutable `items` and `totalAmount` directly from an untrusted client.
    - **Risk:** Direct client manipulation of purchase value or items, leading to financial fraud (customer pays less, gets more; customer pays less, gets accurate inventory; customer disputes charge).
    - **Impact:** High. Potential for significant monetary loss, reputational damage, and operational overhead from fraud investigation.
2.  **Product Data Inconsistency (Lost Sales / Customer Frustration):**
    - **Problem:** The `product-write` service, the authoritative source for product modifications, does not publish events (as verified by code analysis).
    - **Risk:** Read-optimized services (`product-read`, `product-lookup-rust`, future search) operate on stale data, potentially displaying incorrect prices, descriptions, or availability.
    - **Impact:** Medium-High. Negative customer experience (e.g., "price changed at checkout"), customer churn, lost sales due to misinformation.
3.  **Checkout Financial Inaccuracy (Disputes / Operational Cost):**
    - **Problem:** The checkout saga lacks an authoritative, integrated mechanism for real-time price calculation, discount application, and tax calculation _before_ payment.
    - **Risk:** Incorrect final order totals, leading to customer disputes, chargebacks, and significant operational cost for financial reconciliation.
    - **Impact:** High. Direct financial loss, customer dissatisfaction, increased customer service load.
4.  **Limited Product Discovery (Lost Conversion):**
    - **Problem:** No dedicated solution for advanced full-text or semantic product search.
    - **Risk:** Inability for customers to efficiently find desired products on a growing catalog.
    - **Impact:** Medium. Reduced sales conversion rates, poor user experience.

### 1.3. Opportunity

By implementing these architectural changes, we shift from reactively addressing problems to proactively building a resilient, trusted, and scalable platform that drives business value through enhanced customer experience and operational efficiency.

---

## 2. Success Metrics

Our success will be quantitatively and qualitatively measured against the following metrics, which directly address the identified risks:

- **Checkout Fraud Rate:** Reduce client-side initiated fraud attempts (e.g., `totalAmount` manipulation) to **<0.01% of all transactions**.
- **Checkout Completion Rate:** Improve the end-to-end checkout completion rate by **>5%** due to increased reliability, consistent pricing, and reduced payment failures.
- **Order-to-Inventory Discrepancy:** Reduce discrepancies between ordered and available inventory to **<0.05% of all orders**.
- **Product Data Consistency SLA:** Product information served by all read models (`product-read`, `product-lookup-rust`, search) will be consistent with `product-write` within **P99 < 5 seconds** following a write operation.
- **Search Latency:** Achieve **P90 search query response times of <50ms** for full-text queries and **<100ms** for semantic queries.
- **Search Conversion Rate:** Increase search-driven conversion (measured by clicks to purchase after search) by **>10%**.
- **Operational Cost (Checkout Reconciliation):** Reduce manual financial reconciliation efforts for checkout-related discrepancies by **>80%**.

---

## 3. Non-Negotiable Invariants

These are the fundamental, unyielding truths that our system _must_ uphold under all conditions, and which the proposed design explicitly defends.

- **I1: Authorization for Transactional Changes:** No critical business data modification (products, inventory, orders, financial records) shall occur without explicit and valid authorization.
- **I2: Immutable Cart at Checkout Initiation:** The definition of a customer's cart (items, quantities, and their effective prices) for a specific checkout transaction becomes immutable the moment checkout is initiated. All subsequent steps in that transaction must operate on this frozen state.
- **I3: Inventory Atomicity:** A product's inventory level is a protected resource. Stock must be decremented _at most once_ per successful reservation for a given order and _fully restored_ if the transaction fails. No double-booking, over-selling, or lost stock.
- **I4: Authoritative Financial Totals:** All final financial figures (subtotal, discounts, taxes, total) for an order must be calculated by trusted backend services, using immutable product/pricing data, and must be independent of client input.
- **I5: Eventual Consistency Guarantees:** All read-optimized models (product catalog views, search indexes) will eventually reflect the authoritative state committed by write services. Discrepancies may occur briefly but _must_ self-resolve within defined SLAs, and the system must provide mechanisms to validate this.
- **I6: Full Order or Full Compensation:** The system guarantees that a distributed checkout transaction either results in a fully created and paid order (all steps complete) or all prior successful steps are fully and properly compensated (rolled back). Partial, uncompensated transactions are an anti-pattern.

---

## 4. High-Level Architecture

The platform operates as a polyglot microservices architecture, emphasizing domain separation, event-driven communication (Kafka), and orchestration-based sagas for distributed transactions. This diagram illustrates the conceptual flow, not specific component names.

```text
+---------------------+       (HTTP/JSON)           +---------------------+      (HTTP/JSON)           +-------------------------+
|                     | ---------------------------> |                     | ---------------------------> |   Read Model (UI data)  |
|    External Client  |                              |   API Gateway / BFF |                              |   Search API (Discovery)|
| (Web/Mobile App)    |                              |                     |                              |                         |
+---------------------+                              +---------------------+                              +-------------------------+
           ^                                                ^     ^                                                  ^
           |                                                |     | Kafka Event (Checkout Initiation)                | gRPC (Internal Queries)
           |                                                |     |                                                  |
           |                                                v     v                                                  |
           |                           +-------------------------------------------------+                           |
           |                           |                     Kafka Bus                     |<------------------------------------+
           |                           | (Commands, Events for Checkout, Catalog, etc.)  |                                     |
           |                           +-------------------------------------------------+                                     |
           |                                     ^     ^                                                                       |
           |                                     |     | Consume                            Produce                            |
           |                                     |     v                                      ^                                    |
           |                               +----------------------------+                     |                                    |
           |                               |  Saga Orchestrator         |<--------------------+ (Saga State Persistence)           |
           |                               |  (Checkout Coordinator)    |                     |                                    |
           |                               |                            |                     |                                    |
           |                               | - Consumes Kafka Events    |                     |                                    |
           |                               | - Manages Durable Saga State |                     |                                    |
           |                               | - Publishes Kafka Commands |                                                          |
           |                               +----------+-----------------+                                                          |
           |                                          |                                                                          |
           |                                          | (Synchronous gRPC Calls for Pre-Flight Financials)                       |
           |                                          v                                                                          |
           |                 +---------------------+  +---------------------+  +---------------------+  +---------------------+    |
           |                 | Snapshot Service    |<->| Pricing Service     |<->| Discount Service    |<->| Tax Service         |<---| (Internal gRPC Calls)
           |                 +---------------------+  +---------------------+  +---------------------+  +---------------------+    |
           |                           ^                                                                                           |
           |                           |                                                                                           |
           |                 +---------------------+ (Live Cart Data Source)                                                         |
           |                 | Live Cart Service   |<------------------------------------------------------------------------------+
           |                 +---------------------+
           |                           ^                                                                                           |
|                           | (Kafka Commands/Events)                                                                   |
           |                 +---------------------+                                                                               |
           |                 | Inventory Service   | (Stock Reservations)                                                            |
           |                 +---------------------+                                                                               |
           |                           ^                                                                                           |
           |                           | (Kafka Commands/Events)                                                                   |
           |                 +---------------------+                                                                               |
           |                 | Payment Service     | (Process Payments)                                                              |
           |                 +---------------------+                                                                               |
           |                           ^                                                                                           |
           |                           | (Kafka Commands/Events)                                                                   |
           |                 +---------------------+                                                                               |
           |                 | Order Service       | (Final Order Record)                                                            |
           |                 +---------------------+                                                                               |
           |                           ^                                                                                           |
           |                           | (Kafka Events)                                                                            |
           +-----------------------------------------------------------------------------------------------------------------------+
```

---

## 5. Data Model & Consistency Guarantees

Our data model design is driven by domain-driven design principles and the need to uphold consistency invariants.

### 5.1. Key Data Models

- **`Product` (Protobuf):**
  - **Description:** The authoritative data structure for product catalog information (ID, name, description, price, etc.).
  - **Role:** The canonical source for inter-service communication (Kafka event payloads, gRPC messages).
  - **Consistency Anchor:** While `product-write` owns the authoritative record, all downstream read models (`product-read`, `product-lookup-rust`, search index) derive their data from `Product` events.
- **`UserCartSnapshot` (PostgreSQL):**
  - **Description:** An immutable, point-in-time record of a user's cart contents (items, quantities, and their effective prices) captured at the initiation of checkout.
  - **Role:** Guarantees **Invariant I2 (Immutable Cart at Checkout Initiation)**. All subsequent steps of a checkout transaction (pricing, inventory, payment, order creation) refer to this `snapshotId` to retrieve the exact state of the cart when the customer committed to purchase. This prevents mid-transaction changes.
  - **Consistency:** Strong consistency internally, as it's a critical transactional record for the saga.
- **`SagaState` (PostgreSQL):**
  - **Description:** The durable state machine record for each checkout saga instance. Contains `sagaId`, current `state` (e.g., `PAYMENT_PROCESSING_PENDING`), a flexible `context` (JSONB) to hold intermediate data, and `processed_event_ids` for idempotency.
  - **Role:** Essential for **Invariant I6 (Full Order or Full Compensation)** and the orchestrator's ability to recover from failure.
  - **Consistency:** Strong consistency internally to the `checkout-orchestrator` as it governs the saga's progression and rollback.
- **`Order` (PostgreSQL):**
  - **Description:** The authoritative, immutable record of a completed transaction, managed by `order-create`.
  - **Role:** Guarantees the final outcome of the transaction, linking customer, products, and payment.

### 5.2. Consistency Guarantees & Justification

The system operates under a principle of **Eventual Consistency (Invariant I5)** for propagating changes from write models to read models via Kafka events, combined with **Strong Consistency** within transactional boundaries.

- **Write Model (Strongly Consistent):** Services like `product-write`, `inventory-write`, `order-create` maintain strong consistency for their own internal data stores (e.g., stock levels, order records). Operations are ACID-compliant within their microservice.
- **Read Model (Eventually Consistent):** Read-optimized services (`product-read`, `product-lookup-rust`, search index) achieve eventual consistency.
  - **Justification:** The performance and scalability benefits of decoupling read and write operations, especially for high-volume reads, outweigh the temporary lag. End-users can tolerate a few seconds delay for product catalog updates to appear in search results, but cannot tolerate inventory discrepancies.
  - **Mechanism:** Changes are propagated via Kafka events from write models.
  - **Guarantees:** The system commits to a maximum eventual consistency window (e.g., P99 < 5 seconds) for core product data, continuously monitored. Mechanisms for reconciling discrepancies (e.g., replaying events, rebuilding read models) are defined in Section 6.

- **Distributed Transaction (Saga - Compensating Consistency):** The checkout saga implements **compensating consistency** for **Invariant I6 (Full Order or Full Compensation)**.
  - **Justification:** Standard distributed transactions (2PC) are not feasible or scalable in a microservices environment. Sagas allow for long-running, distributed transactions.
  - **Mechanism:** `checkout-orchestrator` tracks state. If a step fails, it initiates compensating transactions to undo prior successful steps.

---

## 6. Failure Scenarios & Resilience (Production Survivability Argument)

Our system is designed to identify and contain failures, ensuring business continuity and upholding invariants, even under extreme duress. We assume services are maliciously unreliable.

### 6.1. System-Wide Resilience Pillars

- **Idempotency (Invariant I3, I6):** All Kafka consumers will implement robust idempotency using unique event/command IDs (`sagaId` as a correlation key, combined with a unique event ID for each message). This prevents duplicate processing of messages, which Kafka can deliver due to "at least once" semantics.
  - **Justification:** Essential for correctly replaying events, recovering from failures, and ensuring financial and inventory integrity.
  - **Implementation:** `SagaState.processed_event_ids` in `checkout-orchestrator`. Message IDs in Kafka event/command payloads checked by consuming services.
- **Circuit Breakers:** Implemented in critical cross-service calls (Kafka producers, HTTP clients, gRPC clients) to prevent cascading failures.
  - **Justification:** Prevents an unhealthy dependency from bringing down an entire chain of services. Protects against "retry storms."
- **Backpressure Handling:** Mechanisms (e.g., Kafka consumer throttling, queue depth monitoring) to prevent services from being overwhelmed by spikes in upstream traffic.
- **Bulkheading:** Isolating resources for different service functionalities or traffic types to prevent one component's failure from affecting others.
- **Health Checks (`/health` endpoint):** All services expose standard health check endpoints for load balancers and orchestrators.
- **Distributed Tracing (Operational Survivability):** OpenTelemetry integration across all services to provide end-to-end visibility of requests flowing through Kafka and gRPC/HTTP calls. This is critical for understanding "what broke where" at 3 AM.

### 6.2. Checkout Saga Failure Modes (Adversarial Analysis)

The checkout saga is our most critical distributed transaction. We assume failure is inevitable and design for rapid, automated recovery.

| **Scenario**                                          | **Primary Risk & Impact**                                            | **Invariant Protected**               | **Mitigation Strategy**                                                                                                                                                                                                                                                              | **Blast Radius**                                                                                   | **Recovery/Action**                                                                       |
| :---------------------------------------------------- | :------------------------------------------------------------------- | :------------------------------------ | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------- | :---------------------------------------------------------------------------------------- |
| **Client-side Data Tampering**                        | Fraudulent order totals, incorrect items/quantities in final order.  | **I1, I2, I4**                        | **Proposed:** `gateway-bff` only accepts `cartId`/`userId`. `checkout-orchestrator` fetches immutable `UserCartSnapshot` (from `cart-snapshot`) and calculates all financials (via `cart-pricing`, `discount_engine`, `tax_calculation`). Client data is never trusted.              | Confined to initial (invalid) client request. No downstream impact.                                | Automatic rejection.                                                                      |
| **`cart-snapshot` Unavailable/Fails**                 | Checkout cannot proceed, operates on potentially mutable/stale data. | **I2**                                | `checkout-orchestrator`'s synchronous gRPC/HTTP call to `cart-snapshot` fails. Orchestrator immediately marks saga `FAILED`.                                                                                                                                                         | Confined to single checkout attempt. No external impact beyond customer's current session.         | Automatic failure. Customer retries or contacts support.                                  |
| **Financial Service (e.g., `discount_engine`) Fails** | Incorrect final `totalAmount` due to failed calculation.             | **I4**                                | `checkout-orchestrator`'s synchronous gRPC/HTTP call to `discount_engine` fails. Orchestrator immediately marks saga `FAILED`.                                                                                                                                                       | Confined to single checkout attempt. No financial risk.                                            | Automatic failure. Customer retries or contacts support.                                  |
| **`inventory-write` Failure (No Stock/DB Error)**     | Customer charged for unavailable product (oversell) / Lost stock.    | **I3**                                | `inventory-write` publishes `InventoryReservationFailedEvent`. `checkout-orchestrator` consumes, marks saga `FAILED`. No payment authorized.                                                                                                                                         | Confined to single checkout. No financial impact as payment is not authorized.                     | Automatic failure. Customer retries or contacts support. Inventory levels remain correct. |
| **`payment-gateway` Fails (Auth/External Provider)**  | Customer cannot complete purchase / Charged without order.           | **I6**                                | `payment-gateway` publishes `PaymentFailedEvent`. `checkout-orchestrator` consumes, marks saga `COMPENSATING`. **Compensation Initiated:** Publishes `CompensateInventoryCommand` to `inventory-write` to release reserved stock.                                                    | Confined to single checkout. No monetary loss. Inventory restored.                                 | Automatic compensation. Customer retries or contacts support.                             |
| **`order-create` Fails (DB Error after Payment)**     | Payment authorized, but no order created.                            | **I6**                                | `order-create` publishes `OrderCreationFailedEvent`. `checkout-orchestrator` consumes, marks saga `COMPENSATING`. **Compensation Initiated:** Publishes `CompensatePaymentCommand` to `payment-gateway` (to refund/void auth) and `CompensateInventoryCommand` to `inventory-write`. | Confined to single checkout. Inventory restored. Payment reversed.                                 | Automatic compensation. Customer is not charged for uncreated order.                      |
| **`checkout-orchestrator` Crash/Restart**             | Saga stops progressing, potential for state inconsistencies.         | **I6**                                | `KafkaConsumerManager` (idempotency + offset management) ensures processing resumes from last successfully processed message. `SagaState` recovered from durable PostgreSQL DB.                                                                                                      | Affects multiple in-flight sagas, but eventual consistency and recovery guaranteed.                | Automatic recovery upon restart. Sagas continue from where they left off.                 |
| **Kafka Broker Outage**                               | Producers cannot publish, consumers cannot consume.                  | System-wide communication disruption. | Kafka is highly available (replicated topics, multiple brokers). Producers use circuit breakers to avoid overwhelming Kafka. Consumers retry connecting.                                                                                                                             | Temporary degradation of event-driven flows. Saga state preserved in DB for orchestrator recovery. | Automated Kafka recovery. Services will resume processing once connectivity is restored.  |
| **Duplicate Kafka Message Delivery**                  | Duplicate inventory reservation, double charge, duplicate order.     | **I3, I4, I6**                        | **Idempotency checks** (G2) in `checkout-orchestrator` and all participating services (e.g., `inventory-write`, `payment-gateway`) consuming commands/events.                                                                                                                        | None, handled by idempotency.                                                                      | Automatic. Message is processed only once.                                                |
| **Unresponsive Participating Service**                | Saga gets stuck in a pending state, never completes.                 | **I6**                                | **Saga Timeout Mechanism (Proposed):** `checkout-orchestrator` sets a timeout for each step. If no response event is received within the timeout, it marks the saga as failed and initiates compensation for prior steps.                                                            | Affects individual stuck sagas.                                                                    | Automated compensation after timeout. Operator intervention for service in question.      |

---

## 7. Scaling Analysis (Throughput Proof)

This section grounds our architectural choices in quantitative analysis, demonstrating the system's feasibility under load.

### 7.1. Assumptions for Scale

- **Peak Traffic:** We assume a peak traffic of **10,000 checkout initiations per second** during flash sales or critical periods.
- **Checkout Complexity:** Each checkout involves 4 synchronous pre-flight calls and 3 asynchronous Kafka command/event cycles (Inventory, Payment, Order, Cart).
- **Event Size:** Average Kafka event/command payload size: **~1KB** (Protobuf binary).
- **Latency Budget:**
  - Synchronous gRPC calls: P99 < 10ms.
  - Asynchronous Kafka message processing: P99 < 50ms per step.

### 7.2. Throughput Math

#### a. Kafka Throughput

- **Checkout Initiation:** 10,000 requests/sec produce 1 `CheckoutInitiatedEvent`.
- **Orchestrator Commands/Events (Happy Path):** Each saga generates roughly 6-8 Kafka messages (1 initiated + 3 commands + 3-4 events back from participants).
- **Total Events/Commands per second:** 10,000 checkouts/sec \* ~8 messages/checkout = **~80,000 messages/sec (peak)**.
- **Kafka Ingress/Egress:** 80,000 messages/sec \* 1KB/message = **~80MB/sec** (both ingress and egress for all topics combined).
- **Feasibility:** Modern Kafka clusters (e.g., Confluent Cloud, self-managed with proper tuning) can easily handle hundreds of MB/sec, making this feasible. Kafka partitions are key to horizontal scaling.

#### b. Orchestrator Load

- **Kafka Consumption:** `checkout-orchestrator` consumes 80,000 messages/sec (on `checkout.checkout-events` and other reply topics).
- **Saga State Reads/Writes:** Each message requires 1 DB read + 1 DB write to update `SagaState`.
  - **Total DB Ops:** ~80,000 reads/sec + ~80,000 writes/sec = **~160,000 DB operations/sec (peak)** on its `SagaState` database.
- **Pre-Flight Synchronous Calls:** 10,000 checkouts/sec \* ~4 gRPC calls/checkout = **~40,000 gRPC calls/sec (peak) outbound**.
- **Feasibility:** Requires a highly performant PostgreSQL cluster (e.g., Amazon Aurora, sharded Postgres) for `SagaState` and robust gRPC client/server implementations (which Python's `asyncio` and FastAPI can support with proper tuning).

#### c. Participating Services Load

- Each participating service (`inventory-write`, `payment-gateway`, `order-create`, `cart-crud`) will experience a peak load of ~10,000 Kafka commands/events per second.
- **Feasibility:** Requires each service to be horizontally scalable and optimized for its specific business logic and database interactions.

### 7.3. Hot Partition & Ordering Strategy (Kafka)

This addresses core distributed systems challenges and ensures our Kafka design handles specific failure patterns.

- **Problem:** Improper partitioning can lead to "hot" partitions, where a single partition receives disproportionately high traffic, becoming a bottleneck. Incorrect ordering can lead to logical inconsistencies.

- **Partition Key Strategy:**
  - **All Checkout-Related Topics:** The primary partition key for all `checkout.*` topics will be **`sagaId` (which is the `orderId`)**.
  - **Justification:**
    - **Ordering:** All messages belonging to a single saga (`CheckoutInitiatedEvent`, `ReserveInventoryCommand`, `PaymentProcessedEvent`, etc.) will always go to the same Kafka partition. This guarantees **strict ordering of events/commands _within a single saga_**, which is absolutely critical for correct saga state transitions and compensation logic.
    - **Hot Partition Risk:** This strategy minimizes the risk of a "hot partition." While a single user might initiate multiple checkouts, the `sagaId` is unique per checkout. Even a "celebrity livestream" leading to 10,000 checkouts/sec will distribute the `sagaId`s (UUIDs) randomly across partitions, preventing any single `userId` or `productId` from creating a bottleneck.
  - **Alternative Keys & Rejection:**
    - **`userId`:** If `userId` were the key, a single popular user could create a hot partition. All their sagas would queue up behind that partition, affecting their experience. This violates our availability tenets.
    - **`productId`:** If `productId` were the key, a popular product could create a hot partition, impacting all checkouts involving that product. This violates availability.

- **Replay Semantics:**
  - **Problem:** How do we recover from a bug that corrupts data or allows incorrect processing?
  - **Strategy:**
    - **Event Retention:** All Kafka topics (especially `checkout.checkout-events` and `catalog.product-events`) will have a minimum retention of **7 days** (or configurable via `KAFKA_RETENTION_DAYS`). Critical events may be archived to S3 for longer-term retention.
    - **Read Model Rebuild:** In case of a bug in a read model (e.g., `product-read`, search index), the service can be shut down, its database cleared, and then restarted to **replay all relevant events from the beginning** of the Kafka topic. This allows a read model to be completely rebuilt.
    - **Saga Re-run:** If a bug in the `checkout-orchestrator` causes sagas to fail incorrectly, a specific tool could be developed to re-run specific sagas by publishing their `CheckoutInitiatedEvent` again, relying on idempotency in downstream services.
    - **Compensation for Financial Actions:** For financial corruptions (e.g., incorrect discount applied for 8 hours), a defined operational playbook will detail how to:
      1.  Stop offending services.
      2.  Identify affected transactions (via logs, tracing, saga history).
      3.  Manually publish compensation commands (e.g., `CompensatePaymentCommand`) for each affected order, leveraging the existing saga compensation mechanism.

---

## 8. Multi-Region Failure Strategy

We design for regional resiliency, assuming an entire AWS/GCP region can become unavailable.

- **Problem:** Region dies during payment authorization. Customer refreshes page.
- **Strategy:** Active-Passive (with potential for Active-Active for read-heavy components).
  - **Saga Ownership:** A single primary region will own all in-flight sagas for a given time. The `checkout-orchestrator` will only run in the primary region.
  - **Data Replication:** Critical data (e.g., `SagaState` in PostgreSQL, `UserCartSnapshot`) will be asynchronously replicated to a standby region.
  - **Kafka:** A multi-region Kafka setup (e.g., MirrorMaker) will replicate critical topics between regions.
  - **DNS Failover:** DNS will be configured to automatically fail over traffic to the standby region if the primary region becomes unhealthy.
  - **Payment Provider Retries:** `payment-gateway` must implement robust, idempotent retries with external payment providers.
  - **Order Duplication Prevention:** The `order-create` service will use transaction IDs (from payment provider) and `sagaId` for strict idempotency to prevent duplicate order creation if a customer retries after a failover.
- **Impact on Invariant I6:** This strategy ensures that even if a region dies, sagas will eventually complete or be fully compensated, preventing partial transactions across regions.

---

## 9. Migration Plan

Transitioning from the current system to the proposed architecture will be phased to minimize downtime and risk.

- **Phase 1: Incremental Feature Rollout:**
  - **Protobuf for Kafka (G6):** Introduce Protobuf alongside JSON for Kafka messages. Services will initially consume both, then migrate to Protobuf-only.
  - **Product Event Publishing (G4):** Implement `product-write` event publishing first.
  - **Search Architecture (G5):** Deploy `search-index` and `search-query` as new capabilities, running in parallel with existing product lookup, gradually shifting traffic.
- **Phase 2: Checkout Rearchitecture (High Risk, Phased Rollout):**
  - **Immutable Cart (`cart-snapshot`):**
    - Implement `cart-snapshot` service.
    - **Dark Launch:** Initially, modify `checkout-orchestrator` to _call_ `cart-snapshot` but ignore its output. Monitor performance and errors.
    - **Canary Deployment:** Gradually roll out changes to `gateway-bff` and `checkout-orchestrator` (starting with a small percentage of users) to use the new secure checkout flow.
  - **Financial Calculations (G3):** Integrate `cart-pricing`, `discount_engine`, `tax_calculation` into the `checkout-orchestrator` as part of the phased checkout rollout.
- **Data Migration:**
  - Existing product data will be indexed into the new search cluster (full re-index).
  - No significant data migration for sagas, as the orchestrator starts clean.

---

## 10. Cost Model

This architecture makes deliberate choices that impact operational costs. Justifying these costs against business value is crucial.

- **Kafka (CRITICAL DRIVER):**
  - **Cost Factor:** Data ingress/egress, storage retention.
  - **Justification:** High scalability, decoupling, fault tolerance, backbone of event-driven architecture. Unavoidable cost for distributed transactions.
  - **Mitigation:** Optimize message sizes (Protobuf), enforce strict topic retention policies (7 days for transient, longer for audit logs).
- **Elasticsearch (CRITICAL DRIVER):**
  - **Cost Factor:** Compute for indexing/querying, storage for indexed data.
  - **Justification:** Essential for G5 (Scalable Search Capabilities). Enables advanced product discovery, directly impacting revenue.
  - **Mitigation:** Optimize indexing strategy, efficient query design, lifecycle management for old indices.
- **`checkout-orchestrator` (High Compute/DB Ops):**
  - **Cost Factor:** High DB read/write IO for `SagaState` (160k ops/sec peak), CPU for processing Kafka messages and orchestrating calls.
  - **Justification:** Core of distributed transaction integrity (I6).
  - **Mitigation:** Optimize DB schema, connection pooling, efficient code.
- **gRPC/HTTP API Services (Standard Compute):**
  - **Cost Factor:** Standard compute resources (CPU, Memory).
  - **Justification:** Provides specific business logic.
  - **Mitigation:** Horizontal scaling, efficient code.

**Overall Economic Viability:** The increased operational costs for Kafka and Elasticsearch are justified by the direct revenue impact of improved search conversion, reduced fraud, and the avoidance of significant operational overhead due to manual reconciliation and customer disputes in a non-resilient system.

---

## 11. Tradeoffs & Rejected Alternatives

This section explicitly defends design choices by articulating rejected alternatives and their drawbacks.

- **Tradeoff: Orchestration-based Saga vs. Choreography-based Saga:**
  - **Choice:** Orchestration-based Saga (with `checkout-orchestrator`).
  - **Why Chosen:** For complex, business-critical flows like checkout, orchestration provides clearer control, centralized monitoring of saga state, and simpler implementation of robust compensation logic. This directly supports **Invariant I6**.
  - **Why Choreography Rejected:** While more decoupled, choreography makes it extremely difficult to track the overall saga state, debug failures, and guarantee full compensation for long-running, multi-step transactions. The risk of partial, uncompensated transactions is too high.
- **Tradeoff: Synchronous gRPC for Pre-Flight Checks vs. Asynchronous Kafka:**
  - **Choice:** Synchronous gRPC calls for initial financial calculations (pricing, discount, tax).
  - **Why Chosen:** Provides immediate feedback for a sequential series of fast "pre-flight" checks, reducing latency for the critical initial phase of the saga. Supports **Invariant I4**.
  - **Why Kafka Rejected:** Using Kafka for each calculation step would introduce unnecessary latency (event publishing/consuming overhead) and increase complexity for operations that require immediate results.
- **Tradeoff: Immutable Cart Snapshot vs. Re-reading Live Cart:**
  - **Choice:** Immutable `UserCartSnapshot` (G2).
  - **Why Chosen:** Guarantees **Invariant I2**. Critical for security (G1) and accuracy (G3). Eliminates race conditions where the live cart changes mid-checkout.
  - **Why Rejected:** Re-reading the live cart would expose the system to race conditions, potential for client-side data tampering, and inconsistent order totals.
- **Tradeoff: Eventual Consistency for Read Models vs. Strong Consistency:**
  - **Choice:** Eventual consistency for read models (G4, I5).
  - **Why Chosen:** Enables massive scalability for read-heavy operations (UI, search) by decoupling them from transactional writes. Performance benefits outweigh the temporary lag.
  - **Why Rejected:** Strong consistency across all microservice data stores in a distributed system is extremely complex, expensive, and often leads to performance bottlenecks. The business tolerates brief inconsistency for read models.

---

## 12. Operational Playbook (What to do at 3 AM)

This section outlines the immediate actions and operational responses to common failure modes, ensuring the system can be run safely without the original authors.

- **Issue:** `checkout-orchestrator` is not processing Kafka messages (consumer lag increasing).
  - **Action:**
    1.  Check `checkout-orchestrator` container health (`kubectl get pods -n <namespace>`). Restart if unhealthy.
    2.  Check `checkout-orchestrator` logs for critical errors (e.g., DB connection issues, Kafka connection issues, `json.loads` errors).
    3.  Check `postgres_dev` CPU/IO for `SagaState` table.
    4.  Check Kafka broker health and topic status (Kafdrop).
    5.  **If a bug:** Disable traffic to `gateway-bff` `POST /api/checkout/initiate`. Manually inspect sagas in `SagaState` table. If data integrity is compromised, initiate replay from specific Kafka offset or compensation for affected sagas.
- **Issue:** Payment failure rate is spiking.
  - **Action:**
    1.  Check `payment-gateway` logs for errors (e.g., external provider timeouts, invalid credentials).
    2.  Check `payment-gateway` container health.
    3.  Check `checkout-orchestrator` logs for `PaymentFailedEvent` handling.
    4.  Alert external payment provider.
- **Issue:** `product-read` (UI display) showing stale product data.
  - **Action:**
    1.  Check `product-write` logs for successful product updates and Kafka producer errors.
    2.  Check `product-read` Kafka consumer lag on `catalog.product-events`.
    3.  Check `product-read` logs for DB update errors or deserialization errors.
    4.  If data corruption is suspected, initiate a read model rebuild by clearing `product-read`'s DB and restarting the service to replay `catalog.product-events`.
- **Issue:** Search results are irrelevant/missing.
  - **Action:**
    1.  Check `search-index` Kafka consumer lag on `catalog.product-events`.
    2.  Check `search-index` logs for indexing errors (e.g., Elasticsearch connection issues).
    3.  Check Elasticsearch cluster health.
    4.  Check `search-query` logs for errors in querying Elasticsearch.
    5.  Initiate a full search index rebuild from Kafka events if necessary.

---

## 13. Open Questions & Future Work

- **Saga Transactional Outbox Pattern:** Consider implementing the Transactional Outbox pattern for all services publishing events to Kafka to guarantee atomicity between local database transactions and event publishing. This would be a further enhancement to **Invariant I6**.
- **Automated Saga Rollbacks:** Implement a dedicated saga rollback mechanism in `checkout-orchestrator` that can be triggered by external systems or a manual command.
- **Distributed Configuration Management:** Implement a centralized configuration system (e.g., Spring Cloud Config, Consul, Kubernetes ConfigMaps/Secrets) to manage environment-specific configurations more dynamically.
- **Dynamic Scaling Policies:** Implement dynamic scaling for microservices in Kubernetes based on CPU, memory, and Kafka consumer lag.
- **Chaos Engineering:** Introduce controlled chaos (e.g., latency injection, service termination) to test the system's resilience.

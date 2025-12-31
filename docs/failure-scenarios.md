# Failure Scenarios and Resilience

In a distributed microservices architecture, anticipating and handling failures gracefully is paramount for system reliability and user experience. This document outlines common failure scenarios in the community platform and the mechanisms in place (or intended) to address them, particularly focusing on the checkout saga.

## 1. General Microservice Failure Modes

Microservices are subject to various failure types that need to be mitigated:

*   **Service Instance Failure:** A single instance of a microservice crashes, becomes unresponsive, or is shut down.
*   **Network Latency/Partitions:** Communication delays or complete disconnections between services, Kafka, or databases.
*   **Dependency Failure:** An external service (e.g., an external payment gateway), a database, or Kafka becomes unavailable or performs poorly.
*   **Resource Exhaustion:** A service runs out of CPU, memory, disk space, or network capacity.
*   **Code Defects:** Bugs in the application logic leading to crashes or incorrect behavior.

## 2. Failure in Checkout Saga Orchestration

The checkout saga, being a distributed transaction, is particularly vulnerable to failures. The `checkout-orchestrator` is designed to manage these.

### 2.1. `checkout-orchestrator` Failures

*   **During Event Processing (Idempotency):**
    *   **Scenario:** The `checkout-orchestrator` processes a Kafka message, updates its internal state, but crashes *before* it can publish the next command or update the saga state in the database. When it restarts, Kafka might redeliver the same message.
    *   **Mitigation:** The `KafkaConsumerManager` explicitly includes an **idempotency check** (`event_id in saga_state.processed_event_ids`). Events are only processed if their `event_id` hasn't been seen before. This ensures that processing the same event multiple times doesn't lead to incorrect state changes or duplicate commands.
*   **Database Failure:**
    *   **Scenario:** The `checkout-orchestrator`'s PostgreSQL database becomes unavailable.
    *   **Mitigation:** The service would likely become unresponsive or fail to update saga states. Recovery would involve restoring database connectivity. Proper database high-availability (HA) setups (e.g., replication, failover) are crucial infrastructure-level mitigations.
*   **Orchestrator Instance Crash/Restart:**
    *   **Scenario:** A `checkout-orchestrator` instance crashes or is restarted during an active saga.
    *   **Mitigation:** Upon restart, the `KafkaConsumerManager` resumes consuming messages from where it last left off (Kafka offset management). Coupled with idempotency, it can recover its position and continue processing the saga without data loss, assuming its database is intact.

### 2.2. Kafka Failures

*   **Kafka Broker Unavailability:**
    *   **Scenario:** One or more Kafka brokers become unavailable.
    *   **Mitigation:** Kafka is designed for high availability with replication. Producers (`gateway-bff`, `checkout-orchestrator`, participating services) are configured with retries. Consumers (`checkout-orchestrator`, participating services) will attempt to reconnect.
*   **Producer Failure (Circuit Breaker):**
    *   **Scenario:** A service tries to publish to Kafka, but Kafka is unresponsive or heavily degraded. Continuous attempts can worsen the situation or cause the service to block.
    *   **Mitigation:** The `checkout-orchestrator`'s `CheckoutService` and the `gateway-bff`'s `ProducerManager` implement **circuit breakers** (`pybreaker` in Python, custom in TypeScript). If Kafka publishing consistently fails, the circuit "opens," preventing further attempts for a configurable period and allowing Kafka to recover. This protects the service from cascading failures and provides immediate feedback.
*   **Unprocessable Messages (Poison Pills):**
    *   **Scenario:** A Kafka message is malformed or causes a consumer to crash repeatedly.
    *   **Mitigation:** Consumers should implement robust error handling. Potentially, unprocessable messages can be moved to a **Dead Letter Queue (DLQ)** topic for manual inspection and reprocessing, preventing them from blocking the consumer. (Currently not explicitly identified, but a best practice).

## 3. Failure in Participating Services (during Checkout Saga)

This is a core aspect of saga patterns: handling failures in intermediate steps.

```text
       +--------------------+
       | Orchestrator State |
       +--------------------+
             |
             v
       [CHECKOUT_INITIATED] --------> Publish ReserveInventory
             |                             (to Inventory Service)
             v
[INVENTORY_RESERVATION_PENDING]
             |
             +---- (InventoryReservationFailed Event) ----> Saga State: FAILED / COMPENSATING
             |                                                  (Triggers Compensation)
             v
       [INVENTORY_RESERVED] --------> Publish ProcessPayment
             |                             (to Payment Service)
             v
[PAYMENT_PROCESSING_PENDING]
             |
             +---- (PaymentFailed Event) ----> Saga State: COMPENSATING
             |                                   (Triggers Compensation: Release Inventory)
             v
       [PAYMENT_PROCESSED] --------> Publish CreateOrder
             |                             (to Order Service)
             v
[ORDER_CREATION_PENDING]
             |
             +---- (OrderCreationFailed Event) ----> Saga State: COMPENSATING
             |                                          (Triggers Compensation: Release Inventory, Refund Payment)
             v
       [ORDER_CREATED] --------> Publish ClearCart
             |                             (to Cart Service)
             v
[CART_CLEARANCE_PENDING]
             |
             +---- (CartClearanceFailed Event) ----> Saga State: COMPENSATING
             |                                          (Triggers Compensation: Potentially previous steps)
             v
         [COMPLETED]
```

*   **Inventory Reservation Fails:**
    *   **Scenario:** `inventory-write` service fails to reserve items (e.g., out of stock, network error to its DB).
    *   **Mechanism:** The `inventory-write` service publishes an `InventoryReservationFailed` event to `checkout.checkout-events`.
    *   **Orchestrator Reaction:** `checkout-orchestrator`'s `handle_inventory_reservation_failed` method receives this, marks the saga as `FAILED` or `COMPENSATING`, and logs the error. At this early stage, compensation might not be required if no resources were allocated yet.
*   **Payment Processing Fails:**
    *   **Scenario:** `payment-gateway` service fails to process payment (e.g., invalid card, external provider error).
    *   **Mechanism:** `payment-gateway` publishes a `PaymentFailed` event to `checkout.checkout-events`.
    *   **Orchestrator Reaction:** `checkout-orchestrator`'s `handle_payment_failed` method marks the saga as `COMPENSATING`. It then publishes a `CompensateInventory` command to `checkout.inventory-command`, initiating the rollback of the inventory reservation.
*   **Order Creation Fails:**
    *   **Scenario:** `order-create` service fails to create the order (e.g., DB error, business rule violation).
    *   **Mechanism:** `order-create` publishes an `OrderCreationFailed` event to `checkout.checkout-events`.
    *   **Orchestrator Reaction:** `checkout-orchestrator`'s `handle_order_creation_failed` method marks the saga as `COMPENSATING`. It then publishes *both* `CompensatePayment` and `CompensateInventory` commands to their respective topics to roll back all prior successful steps.
*   **Cart Clearance Fails:**
    *   **Scenario:** `cart-crud` service fails to clear the cart (e.g., DB issue).
    *   **Mechanism:** `cart-crud` publishes a `CartClearanceFailed` event to `checkout.checkout-events`.
    *   **Orchestrator Reaction:** `checkout-orchestrator`'s `handle_cart_clearance_failed` method marks the saga as `COMPENSATING`. Depending on the design, this might trigger compensation for earlier steps if not already handled.

## 4. Compensation Logic

When a saga step fails, the `checkout-orchestrator` coordinates **compensating transactions** to undo the effects of previous successful steps.

*   **Mechanism:** The orchestrator publishes specific "compensate" commands (e.g., `CompensateInventory`, `CompensatePayment`) to the command topics of the relevant services.
*   **Participating Service Role:** Each participating service must be able to:
    *   Consume these compensation commands.
    *   Execute the inverse operation (e.g., release reserved inventory, refund a payment).
    *   Publish a `*Compensated` event to `checkout.checkout-events` to inform the orchestrator of the compensation status.

## 5. Other Resilience Mechanisms

*   **Health Checks (`/health` endpoint):** FastAPI services (like `checkout-orchestrator`) expose a `/health` endpoint allowing load balancers and container orchestrators (like Kubernetes) to determine if a service instance is healthy and responsive.
*   **Logging & Metrics:** Comprehensive logging and metrics (e.g., Prometheus `generate_latest` in `checkout-orchestrator`) are crucial for detecting and diagnosing failures.
*   **Alerting:** Setting up alerts on critical metrics (e.g., high error rates, long latencies, saga failures) to notify operators quickly.
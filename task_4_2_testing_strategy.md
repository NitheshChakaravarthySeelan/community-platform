## Task 4.2: Comprehensive Performance and Stress Testing Strategy

This document outlines a strategy for conducting comprehensive performance and stress tests to verify the system's performance and robustness goals. While the implementation of the testing itself is outside the scope of this agent's direct execution due to infrastructure constraints, this strategy provides the necessary steps and considerations for a successful testing phase.

**1. Verification Goals (from User Request & Master Design):**

- **Rust (product-lookup-rust):** Achieve 3.1x faster p99 latency for critical lookup operations.
- **Python (checkout-orchestrator):** Achieve 45% higher throughput for I/O-bound operations (event-driven saga).
- **Java (product-read):** Achieve ~70% lower p50 latency and 92% cache-hit rate for catalog data.
- **AI (plan-executor):** Achieve >90% successful autonomous transaction completion.
- **Overall System:** Enhanced resilience, consistency, and adaptability through advanced patterns (idempotency, circuit breakers).

**2. Recommended Load Testing Tools & Why:**

- **Locust (Python):** Ideal for simulating complex user behavior and business flows (e.g., a full checkout journey, autonomous shopping scenarios). Its Python scripting allows for flexible scenario definition and integration with Python-based AI services.
  - _Usage:_ Define `TaskSet` classes for user types, specifying wait times, API calls (HTTP/gRPC/Kafka events).
- **k6 (JavaScript/Go):** Excellent for API load testing with a focus on scripting and metric collection. It's lightweight and can be easily integrated into CI/CD. Good for specific endpoint stress tests.
  - _Usage:_ Script API calls to specific services (e.g., `product-read` endpoints, `intent-parser`).
- **JMeter (Java):** A powerful, feature-rich tool for comprehensive performance testing of various protocols (HTTP, JDBC, JMS - useful for Kafka). Can be used for more detailed protocol-level testing.
  - _Usage:_ For load testing Java services and potentially Kafka directly (via JMS samplers).
- **wrk (HTTP) / grpc-perf (gRPC):** High-performance command-line tools for raw throughput and latency measurements. Excellent for quickly establishing baseline performance for single endpoints.
  - _Usage:_ For quick, focused benchmarks on `product-read` (REST), `product-lookup-rust` (gRPC), `intent-parser` (REST), `plan-generator` (REST), `plan-executor` (REST).

**3. Test Environment Setup (User's Responsibility to Provision):**

- **Production-like Environment:** Testing should occur in an environment as close to production as possible (e.g., Kubernetes cluster, or a robust Docker Compose setup).
- **Load Test Infrastructure:** Dedicated machines/pods for running load generators, separate from the application under test, to avoid resource contention.
- **Monitoring Stack:** A fully configured Prometheus, Grafana, and Jaeger stack (as outlined in `infra/docker/docker-compose.dev.yml`) is crucial for collecting and visualizing metrics.

**4. Defining Test Scenarios/Workloads:**

- **Baseline Performance:**
  - Execute load tests _before_ deploying any of the performance improvements to establish current metrics. This is critical for quantifying gains.
- **Read-Heavy Catalog Browsing (`product-read`, `product-lookup-rust`):**
  - **Goal:** Verify ~70% p50 latency reduction and 92% Redis Cache Hit Rate for `product-read`, and 3.1x faster p99 latency for `product-lookup-rust`.
  - **Scenario:** Simulate a large number of users browsing product pages, searching, and viewing product details.
  - **Workload:**
    - 80% `GET /api/products/{id}` (targeting cached items and hitting `product-lookup-rust` when integrated).
    - 20% `GET /api/products` (targeting all products list, potentially cached).
  - **Tools:** k6 or Locust.
- **Checkout Flow (`checkout-orchestrator` - Event-Driven Saga):**
  - **Goal:** Verify 45% higher throughput for `checkout-orchestrator` (after async migration and saga implementation).
  - **Scenario:** Simulate users initiating a checkout, followed by the asynchronous progression of the saga.
  - **Workload:**
    - `POST /api/checkout` (initiates the saga).
    - Simulate subsequent Kafka events from other services (Inventory, Payment, Order) to drive the saga. This might require custom test agents or direct Kafka topic publishing in the test script.
  - **Tools:** Locust (for initiating checkout), custom Kafka producers/consumers (for simulating event flow if needed).
- **Autonomous Shopping (`intent-parser`, `plan-generator`, `plan-executor`):**
  - **Goal:** Verify >90% successful autonomous transaction completion.
  - **Scenario:** Simulate users submitting natural language queries that lead to complex multi-step actions (e.g., "Add 5 red shirts to cart and check out").
  - **Workload:**
    - `POST /parse-intent` (to `intent-parser`).
    - `POST /generate-plan` (to `plan-generator`).
    - `POST /execute-plan` (to `plan-executor`).
  - **Tools:** Locust or k6.
- **Stress Testing:**
  - Gradually increase load beyond expected peak to identify breaking points, bottlenecks, and error modes.
- **Resilience Testing:**
  - Introduce failures (e.g., stop a downstream service, introduce network delays, induce Kafka broker failure) and observe system behavior:
    - Does the saga recover?
    - Are compensating transactions initiated correctly?
    - Do circuit breakers trip and gracefully degrade service?

**5. Metric Collection and Analysis:**

- **Prometheus & Grafana:**
  - Set up Grafana dashboards to visualize:
    - API Latency (p50, p95, p99) for all critical endpoints.
    - API Throughput (Requests Per Second - RPS).
    - Error Rates (HTTP 5xx, Kafka message failures).
    - Resource Utilization (CPU, Memory, Network I/O) for all services and Kafka/Redis/Postgres.
    - Kafka Consumer Lag for all consumer groups.
    - Redis Hit Rate for `product-read`.
    - Autonomous Transaction Completion Rate (custom metric from `plan-executor` or `checkout-orchestrator`).
- **Jaeger (Distributed Tracing):**
  - Crucial for understanding end-to-end latency and identifying bottlenecks in distributed transactions. Use trace IDs to link events across services.
  - Verify that custom spans for saga steps are correctly instrumented.
- **Load Testing Tool Reports:** Analyze detailed reports from Locust/k6 for request durations, error rates, and response data.

**6. Iterative Improvement:**

- Performance testing is an iterative process. Identify bottlenecks, optimize code/configuration, and re-test until goals are met.

This strategy provides a comprehensive framework for the user to verify the implemented performance and resilience improvements across the system. The successful execution of this strategy will confirm that all architectural and performance goals have been met.

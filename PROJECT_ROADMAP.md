# Project Roadmap: From Core Implementation to Production Deployment

**Version:** 1.0
**Status:** In Progress

---

## 1. Introduction & Guiding Principles

This document is the master roadmap for the development, testing, and deployment of the community platform. It consolidates all previous planning documents (`NextStep.md`, `MASTER_SYSTEM_DESIGN.md`, etc.) into a single, actionable guide. Our goal is to build a hyper-scalable, resilient, and AI-driven e-commerce platform based on a composable commerce architecture.

### 1.1. Core Architectural Tenets

All development must adhere to these non-negotiable principles as defined in the master system design:

1.  **Domain-Driven Design (DDD):** Services are aligned with business domains.
2.  **Database per Service:** Each microservice has exclusive ownership of its data.
3.  **Asynchronous-First Communication:** Kafka is the default for inter-service communication to ensure decoupling and resilience. Synchronous calls (gRPC/REST) are the exception.
4.  **API-First Contracts:** OpenAPI 3.0 for REST, Protobuf 3 for gRPC.
5.  **Infrastructure as Code (IaC):** All infrastructure is defined in Terraform.
6.  **Zero Trust Security:** No service implicitly trusts another; mTLS will be used for all internal communication.
7.  **Immutability:** Infrastructure and containers are replaced, not patched.

### 1.2. Key Performance Goals (SLOs)

- **Availability:** `99.99%` for critical APIs (Checkout, Product Details, Login).
- **p99 Latency:** `< 150ms` for user-facing reads; `< 300ms` for writes.
- **Scalability:** Handle a 10x traffic surge within 5 minutes.

---

## 2. Phased Implementation Plan

This plan is broken into sequential phases. Each phase builds upon the last, moving from stabilizing the core application to implementing advanced features and finally, deploying to production.

### Phase 1: Solidify the Core & Fix CI/CD

**Goal:** Achieve a stable, buildable, and testable foundation with a fully "green" CI pipeline. The core checkout saga will be functionally complete.

#### **Task 1.1: Finalize Rust Service (`inventory-write`) Build**

- **Objective:** Enable the `inventory-write` service to be built reliably in any environment, especially CI, without a live database connection.
- **Problem:** The `sqlx` crate's `query_as!` macro performs compile-time checks against a live database, causing the build to fail in environments where the database isn't running.
- **Solution (Step-by-Step):**
  1.  **Ensure DB is Running:** Start the PostgreSQL container for a one-time setup:
      ```bash
      docker-compose -f infra/docker/docker-compose.dev.yml up -d postgres
      ```
  2.  **Create Schema:** The database table is missing. Connect to the running container and create the necessary tables.

      ```bash
      # Create the inventory_items table
      docker-compose -f infra/docker/docker-compose.dev.yml exec -T postgres psql -U admin -d community_platform -c "CREATE TABLE inventory_items (id UUID PRIMARY KEY, product_id UUID UNIQUE NOT NULL, quantity INTEGER NOT NULL, created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL);"

      # Create the inventory table used by the Kafka consumer
      docker-compose -f infra/docker/docker-compose.dev.yml exec -T postgres psql -U admin -d community_platform -c "CREATE TABLE inventory (product_id VARCHAR(255) PRIMARY KEY, quantity INTEGER NOT NULL);"
      ```

  3.  **Run `sqlx prepare`:** Execute the command in the service's directory. This connects to the database and generates a `sqlx-data.json` file.
      ```bash
      cd services/inventory/inventory-write && cargo sqlx prepare
      ```
  4.  **Verification:** The `sqlx-data.json` file will be created. Subsequent `cargo build` commands will now use this file for compile-time checks, succeeding even if the database is offline.

#### **Task 1.2: Complete the Event-Driven Checkout Saga**

- **Objective:** Make the checkout flow fully event-driven as designed, removing all synchronous HTTP calls between services in the saga.
- **LLD Details:**
  1.  **`payment-gateway` Consumer:**
      - **Topic:** `checkout.payment-command`
      - **Logic:** Consume the `ProcessPayment` command. Simulate a payment attempt (e.g., with a 90% success rate).
      - **Produce Event:** Based on the outcome, publish either a `PaymentProcessed` or `PaymentFailed` event to the `checkout.checkout-events` topic.
  2.  **`order-create` Consumer:**
      - **Topic:** `checkout.order-command`
      - **Logic:** Consume the `CreateOrder` command. Create a new order record in its PostgreSQL database.
      - **Produce Event:** Publish an `OrderCreated` event with the new `order_id` or an `OrderCreationFailed` event to the `checkout.checkout-events` topic.
  3.  **`cart-crud` Consumer:**
      - **Topic:** `checkout.cart-command`
      - **Logic:** Consume the `ClearCart` command, which is the final step in a successful saga.
      - **Produce Event:** Publish a `CartCleared` event to confirm success or `CartClearanceFailed` on error.

#### **Task 1.3: Implement Saga Compensation Logic**

- **Objective:** Make the checkout saga resilient to failure by implementing rollback procedures.
- **LLD Details:**
  1.  **`inventory-write` Compensation:**
      - **Topic:** `checkout.inventory-command`
      - **Logic:** Implement a consumer for a `CompensateInventory` command. This should reverse the inventory reservation (i.e., add the stock back). This logic must be idempotent.
  2.  **`payment-gateway` Compensation:**
      - **Topic:** `checkout.payment-command`
      - **Logic:** Implement a consumer for a `CompensatePayment` command. This should trigger a refund for the given transaction. For now, this can be a log message stating "Refund processed for saga ID...".

#### **Task 1.4: CI Pipeline Validation**

- **Objective:** Achieve a "green" build for the entire project on GitHub Actions.
- **Action:** After the previous tasks are complete, manually trigger the `ci.yml` workflow and diagnose any remaining failures.
- **Rationale:** A stable CI pipeline is the gatekeeper of quality and the first step towards automated deployments. With the build, dependency, and formatting issues resolved, the pipeline should now pass.

---

### Phase 2: Implement Missing Services

**Goal:** Flesh out the placeholder services with a "Minimum Viable Implementation" so they are functional, deployable components.

#### **Task 2.1: Scaffold Notification Services (`email-service`, `sms-service`, `push-service`)**

- **Objective:** Create basic, working services that can receive notification requests.
- **LLD for each service:**
  1.  **Framework:** Choose a lightweight framework (e.g., Express for Node.js, Flask/FastAPI for Python, or a simple Go HTTP server).
  2.  **Kafka Consumer:** Implement a consumer to listen on a shared `notifications` topic.
  3.  **API Endpoint:** Create a single `POST /send` endpoint.
  4.  **Logic:** The consumer and endpoint should simply log the received notification request (e.g., `[email-service] INFO: Received request to send email to user_123`). No actual sending logic is needed yet.
  5.  **Dockerfile:** Add a standard `Dockerfile` for the chosen language/framework.
  6.  **Helm Chart:** Fill in the `deployment.yaml` and `service.yaml` in the service's Helm chart directory.

#### **Task 2.2: Scaffold Core Logic Services (`discount-engine`, `tax-calculation`, `refund`)**

- **Objective:** Create mock implementations that can participate in the checkout flow without complex logic.
- **LLD for each service:**
  1.  **`discount-engine`:**
      - **API:** `POST /calculate-discounts`
      - **Logic:** Receives cart details, returns a JSON response like `{"total_discount_cents": 0}`.
  2.  **`tax-calculation`:**
      - **API:** `POST /calculate-tax`
      - **Logic:** Receives cart and address details, returns a fixed amount, e.g., `{"tax_cents": 1000}`.
  3.  **`refund-service`:**
      - **API:** `POST /issue-refund`
      - **Logic:** Receives order/payment details, logs the request, and returns a success response, e.g., `{"refund_id": "uuid-goes-here", "status": "PENDING"}`.

---

### Phase 3: Prepare for Deployment (Hosting on Kubernetes)

**Goal:** Make the entire application deployable and accessible on a Kubernetes cluster.

#### **Task 3.1: Complete Containerization & Helm Charts**

- **Objective:** Ensure every service is containerizable and has a complete Helm chart for deployment.
- **Action:**
  1.  Review every service directory to ensure a working `Dockerfile` exists.
  2.  Systematically go through `infra/helm-charts` and complete the `deployment.yaml`, `service.yaml`, and `values.yaml` for every single service, using the `user-service` chart as a template.
      - **`deployment.yaml`:** Should define the container image, replicas, ports, and environment variables (loaded from ConfigMaps/Secrets).
      - **`service.yaml`:** Should define how the service is exposed internally within the cluster (e.g., `ClusterIP`).

#### **Task 3.2: Local Kubernetes Deployment with Minikube/Kind**

- **Objective:** Validate the entire deployment process on a local machine.
- **Action (Step-by-Step):**
  1.  **Install Tooling:** Install `kubectl`, `helm`, and a local K8s provider like `minikube` or `kind`.
  2.  **Start Cluster:** Run `minikube start` or `kind create cluster`.
  3.  **Build Images:** Run `docker-compose -f infra/docker/docker-compose.dev.yml build` to build local images for all services.
  4.  **Load Images:** Load the locally built images into your cluster (e.g., `minikube image load <image-name>`).
  5.  **Deploy with Helm:** For each service, run `helm install <release-name> infra/helm-charts/<service-name>`.

#### **Task 3.3: Set Up Ingress for External Access**

- **Objective:** Expose the `gateway-bff` to be accessible from your browser.
- **Action (Step-by-Step):**
  1.  **Install Ingress Controller:** Deploy an NGINX Ingress Controller to your local cluster. For Minikube, this is `minikube addons enable ingress`.
  2.  **Create Ingress Resource:** Create a file `ingress.yaml` and apply it with `kubectl apply -f ingress.yaml`.
  - **`ingress.yaml` Example:**
    ```yaml
    apiVersion: networking.k8s.io/v1
    kind: Ingress
    metadata:
      name: main-ingress
    spec:
      rules:
        - http:
            paths:
              - path: /
                pathType: Prefix
                backend:
                  service:
                    name: gateway-bff-service # The name of your BFF's K8s service
                    port:
                      number: 3000 # The port the BFF service exposes
    ```
  3.  **Verification:** Find the IP of your local cluster and access it in a browser. You should see the `gateway-bff`'s landing page.

---

### Phase 4: Advanced Features & Verification

**Goal:** Implement advanced functionality and verify that the system meets its performance targets.

#### **Task 4.1: Performance Testing & Verification**

- **Objective:** Quantitatively verify the SLOs defined in the master design document.
- **Action (Leveraging `task_4_2_testing_strategy.md`):**
  1.  **Choose Tooling:** Use `k6` for scriptable API load testing and `locust` for simulating complex user flows.
  2.  **Write Test Scenarios:**
      - **Read-Heavy:** A `k6` script that continuously hits the `product-read` endpoints.
      - **Checkout Flow:** A `locust` script that simulates a user adding items to a cart and completing a checkout.
  3.  **Execute & Monitor:** Run these tests against your local Kubernetes deployment while monitoring latency (p99) and throughput (RPS) in Prometheus/Grafana. Compare the results against the SLOs.

#### **Task 4.2: AI Service Integration**

- **Objective:** Fully integrate the AI services (`intent-parser`, `plan-generator`, `plan-executor`) using an event-driven approach.
- **Action:**
  1.  Modify `plan-executor` to no longer make direct HTTP calls to `checkout-orchestrator`.
  2.  Instead, when the plan requires a checkout, `plan-executor` should publish an `InitiateCheckoutCommand` event to the appropriate Kafka topic, just as a real frontend client would.
  3.  This makes the AI's interaction with the checkout flow identical to a user's, ensuring consistency and resilience.

This roadmap provides a clear and detailed path forward. I am ready to begin with **Phase 1, Task 1.1**.

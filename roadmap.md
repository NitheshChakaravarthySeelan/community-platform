# Project Development Roadmap: Core Application Build

This roadmap outlines a sequential, service-by-service approach to building the initial version of the Community Platform application. It focuses on the logical order of development, service responsibilities, and their primary interactions, without delving into advanced performance optimizations or event-driven refactoring (which would be subsequent phases).

The goal is to establish a working, albeit initially synchronous, multi-service application that can then be gradually improved upon.

---

## Foundational Infrastructure (Prerequisites)

Before developing individual services, ensure the following foundational infrastructure components are in place and accessible:

1.  **Database(s):**
    - **Purpose:** Persistent storage for all service data. Each service typically manages its own database schema.
    - **Considerations:** PostgreSQL is a common choice. Ensure connection details (host, port, credentials) are available to services.
    - **Setup:** Use `infra/docker/docker-compose.dev.yml` for local development, or deploy to Kubernetes (`infra/k8s`) for staging/production.
2.  **Service Discovery:**
    - **Purpose:** Allows services to find and communicate with each other without hardcoding IP addresses.
    - **Considerations:** In Kubernetes, this is often handled by Kube-DNS. For local Docker Compose, service names resolve to container IPs.
3.  **API Gateway / Load Balancer:**
    - **Purpose:** Entry point for external clients (e.g., `admin-ui`, mobile apps) to access backend services. Handles routing, authentication, and potentially rate limiting.
    - **Considerations:** `apps/gateway-bff` might serve this role for the frontend. For internal service-to-service communication, direct calls or service mesh might be used.
4.  **Containerization (Docker):**
    - **Purpose:** Package each service into a portable, isolated environment.
    - **Setup:** Ensure `Dockerfile`s are defined for each service.
5.  **Orchestration (Kubernetes / Docker Compose):**
    - **Purpose:** Manage deployment, scaling, and networking of containerized services.
    - **Setup:** Use `infra/docker/docker-compose.dev.yml` for local development. For production, `infra/helm-charts` and `infra/k8s` indicate Kubernetes usage.

---

## Service Development Order

This section details the recommended order for developing each microservice, along with its primary responsibilities and initial interactions.

### Phase 1: Core Catalog & User Management

These services form the absolute foundation of any e-commerce platform.

#### 1.1. Catalog Services (Java)

- **Purpose:** Manage product information, brands, and categories.
- **Services:**
  - `services/catalog/brand` (Java)
  - `services/catalog/category` (Java)
  - `services/catalog/product-write` (Java)
  - `services/catalog/product-read` (Java)
- **Development Order:**
  1.  **`brand` & `category`:** Implement basic CRUD (Create, Read, Update, Delete) APIs for managing brands and categories. These are likely independent.
  2.  **`product-write`:** Implement APIs for creating, updating, and deleting product information. This service will manage the authoritative product data.
  3.  **`product-read`:** Implement APIs for retrieving product information (e.g., by ID, by category, search). This service will query the product data managed by `product-write`.
- **Initial Interactions:**
  - `product-read` queries `product-write`'s database (or a read replica) directly, or `product-write` publishes updates to a shared database that `product-read` consumes. For initial build, direct database access or a shared database is simpler.
  - External clients (e.g., `admin-ui`, `gateway-bff`) will call `product-read` for display.
  - `product-write` will depend on `brand` and `category` for associating products.

#### 1.2. User Services (Java)

- **Purpose:** Manage user accounts, authentication, and authorization.
- **Services:**
  - `services/users/user-service` (Java)
  - `services/users/auth-service` (Java)
- **Development Order:**
  1.  **`user-service`:** Implement basic CRUD for user profiles (registration, profile updates, fetching user details).
  2.  **`auth-service`:** Implement user authentication (login, logout, token generation/validation) and authorization (checking user permissions).
- **Initial Interactions:**
  - `auth-service` will interact with `user-service` to verify user credentials and retrieve user details during authentication.
  - External clients (`gateway-bff`) will call `auth-service` for login/registration and `user-service` for profile management.

---

### Phase 2: Shopping Cart & Inventory

Building on core products and users, enable shopping functionality.

#### 2.1. Cart Services

- **Purpose:** Allow users to manage items in their shopping cart.
- **Services:**
  - `services/cart/cart-crud` (TypeScript/Node.js)
  - `services/cart/cart-pricing` (Java)
  - `services/cart/cart-snapshot` (Java)
- **Development Order:**
  1.  **`cart-crud`:** Implement APIs for adding items to a cart, updating quantities, removing items, and retrieving cart contents for a user. This service will manage the current state of user carts.
  2.  **`cart-pricing`:** Implement logic to calculate the total price of a cart, applying basic pricing rules. Initially, this might be a simple calculation based on product prices from `product-read`.
  3.  **`cart-snapshot`:** (Optional for initial build, can be deferred) This service would typically store historical cart data or snapshots for analytics/recovery.
- **Initial Interactions:**
  - External clients (`gateway-bff`) call `cart-crud` for all cart manipulations.
  - `cart-crud` calls `product-read` to get product details (price, name, image) when adding/updating items.
  - `cart-crud` calls `cart-pricing` to get the calculated total for a cart.

#### 2.2. Inventory Services

- **Purpose:** Manage product stock levels and availability.
- **Services:**
  - `services/inventory/inventory-read` (Rust)
  - `services/inventory/inventory-write` (Rust)
  - `services/inventory/warehouse-sync` (Python)
- **Development Order:**
  1.  **`inventory-write`:** Implement APIs for updating stock levels (e.g., adding new stock, decrementing stock during an order). This service manages the authoritative inventory data.
  2.  **`inventory-read`:** Implement APIs for checking product availability and current stock levels. This service queries the inventory data managed by `inventory-write`.
  3.  **`warehouse-sync`:** (Optional for initial build, can be deferred) This service would typically integrate with an external warehouse system to synchronize stock levels.
- **Initial Interactions:**
  - `inventory-read` queries `inventory-write`'s database (or a read replica).
  - `cart-crud` (or `checkout-orchestrator` later) calls `inventory-read` to check if items are in stock before allowing them to be added to cart or purchased.

---

### Phase 3: Order & Checkout Flow (Synchronous Initial Version)

Establish the core transaction flow.

#### 3.1. Order Services (Java)

- **Purpose:** Manage the lifecycle of customer orders.
- **Services:**
  - `services/orders/order-create` (Java)
  - `services/orders/order-read` (Java)
- **Development Order:**
  1.  **`order-create`:** Implement APIs for creating a new order. This service will receive order details (from `checkout-orchestrator`) and persist them.
  2.  **`order-read`:** Implement APIs for retrieving order details (e.g., by order ID, by user ID).
- **Initial Interactions:**
  - `checkout-orchestrator` calls `order-create` to finalize an order.
  - External clients (`gateway-bff`) call `order-read` to display order history.

#### 3.2. Payment Gateway (Java)

- **Purpose:** Process payments for orders.
- **Service:** `services/orders/payment-gateway` (Java)
- **Development Order:**
  1.  **`payment-gateway`:** Implement APIs to initiate and confirm payments with an external payment provider (or a mock provider for initial development).
- **Initial Interactions:**
  - `checkout-orchestrator` calls `payment-gateway` to process the payment for an order.

#### 3.3. Checkout Orchestrator (Python)

- **Purpose:** Coordinate the steps involved in a customer checkout.
- **Service:** `services/checkout/checkout-orchestrator` (Python)
- **Development Order:**
  1.  **`checkout-orchestrator`:** Implement a synchronous API endpoint (e.g., `/checkout`) that:
      - Receives a request to initiate checkout (e.g., with `cart_id`).
      - Calls `cart-crud` to get cart details.
      - Calls `inventory-read` to verify stock for all items in the cart.
      - Calls `payment-gateway` to process payment.
      - Calls `order-create` to create the final order.
      - Calls `cart-crud` to clear the cart.
- **Initial Interactions:**
  - External clients (`gateway-bff`) call `checkout-orchestrator` to start the checkout process.
  - `checkout-orchestrator` makes direct HTTP/RPC calls to `cart-crud`, `inventory-read`, `payment-gateway`, and `order-create`.

---

### Phase 4: Supporting Features & AI Integration (Initial)

Adding more functionality and the first steps towards autonomous shopping.

#### 4.1. Pricing Services (Java)

- **Purpose:** Handle complex pricing logic, discounts, and tax calculations.
- **Services:**
  - `services/pricing/list-price` (Java)
  - `services/pricing/discount-engine` (Java)
  - `services/pricing/tax-calculation` (Java)
- **Development Order:**
  1.  **`list-price`:** Implement APIs to retrieve the base price of products. (This might be integrated into `product-read` initially and then extracted).
  2.  **`discount-engine`:** Implement APIs to apply discounts based on rules (e.g., coupons, promotions).
  3.  **`tax-calculation`:** Implement APIs to calculate taxes based on product type, user location, etc.
- **Initial Interactions:**
  - `cart-pricing` (or `checkout-orchestrator`) calls `list-price`, `discount-engine`, and `tax-calculation` to get the final price for items/cart.

#### 4.2. Notifications Services (Go)

- **Purpose:** Send various notifications to users (email, push, SMS).
- **Services:**
  - `services/notifications/email-service` (Go)
  - `services/notifications/push-service` (Go)
  - `services/notifications/sms-service` (Go)
- **Development Order:**
  1.  **`email-service`:** Implement API to send emails.
  2.  **`push-service`:** Implement API to send push notifications.
  3.  **`sms-service`:** Implement API to send SMS messages.
- **Initial Interactions:**
  - `order-create` (or `checkout-orchestrator`) calls `email-service` to send order confirmation.
  - `user-service` might call these for account-related notifications.

#### 4.3. Search Services (Rust, Python)

- **Purpose:** Enable product search and recommendations.
- **Services:**
  - `services/search/search-index` (Rust)
  - `services/search/search-query` (Rust)
  - `services/search/rec-model-service` (Python)
- **Development Order:**
  1.  **`search-index`:** Implement APIs to build and update a search index (e.g., from `product-read` data).
  2.  **`search-query`:** Implement APIs to perform searches against the index.
  3.  **`rec-model-service`:** (Optional for initial build) Implement APIs for product recommendations based on user behavior or product data.
- **Initial Interactions:**
  - `product-write` (or a dedicated data pipeline) feeds data to `search-index`.
  - External clients (`gateway-bff`) call `search-query` for product search.
  - `search-query` might call `rec-model-service` for personalized results.

#### 4.4. AI Services (Python) - Initial Integration

- **Purpose:** Provide natural language understanding and basic workflow execution.
- **Services:**
  - `services/ai/intent-parser` (Python)
  - `services/ai/plan-generator` (Python)
  - `services/ai/plan-executor` (Python)
- **Development Order:**
  1.  **`intent-parser`:** Implement an API that takes natural language text and returns a structured intent (e.g., "add item", "checkout").
  2.  **`plan-generator`:** Implement an API that takes a structured intent and generates a sequence of actions (a "plan") using core service APIs.
  3.  **`plan-executor`:** Implement an API that takes a plan and executes the corresponding calls to core services (`cart-crud`, `checkout-orchestrator`).
- **Initial Interactions:**
  - An external client (e.g., a chatbot UI) sends natural language to `intent-parser`.
  - `intent-parser` calls `plan-generator`.
  - `plan-generator` calls `plan-executor`.
  - `plan-executor` makes direct HTTP/RPC calls to `cart-crud`, `checkout-orchestrator`, and potentially `product-read`.

---

### Phase 5: Cross-Cutting Concerns & Observability

These services provide essential support for the entire platform.

#### 5.1. Ops Services (Go)

- **Purpose:** Provide monitoring, logging, and tracing capabilities.
- **Services:**
  - `services/ops/config` (Go)
  - `services/ops/log-forwarder` (Go)
  - `services/ops/metrics` (Go)
  - `services/ops/tracing` (Go)
- **Development Order:**
  1.  **`config`:** Implement a centralized configuration service (if not using Kubernetes ConfigMaps/Secrets directly).
  2.  **`log-forwarder`:** Implement a service to collect and forward logs from all other services to a central logging system.
  3.  **`metrics`:** Implement a service to collect and expose application metrics (e.g., Prometheus format).
  4.  **`tracing`:** Implement a service to collect and store distributed traces (e.g., Jaeger).
- **Initial Interactions:**
  - All other services will integrate with these ops services for logging, metrics, and tracing.

#### 5.2. Audit Service (Java)

- **Purpose:** Record significant events and actions for auditing and compliance.
- **Service:** `services/audit/audit-service` (Java)
- **Development Order:**
  1.  **`audit-service`:** Implement an API to receive and persist audit logs.
- **Initial Interactions:**
  - Key services (e.g., `auth-service`, `order-create`, `user-service`) will call `audit-service` to log important events.

---

## Mapping Structure & Communication

Initially, most service-to-service communication can be implemented using **synchronous HTTP/REST APIs** or **gRPC** (especially for Rust/Java/Python where Protobuf is easy to integrate).

- **API Gateway (`gateway-bff`):** Acts as the primary entry point for external clients, routing requests to the appropriate backend services.
- **Direct Service-to-Service Calls:** Services will call each other directly via their exposed HTTP/gRPC endpoints.
- **Shared Libraries (`shared/libs/ts`, `shared/libs/java`, `shared/proto`):** Use these for common data models, API contracts (Protobuf definitions), and utility functions to ensure consistency across languages.

**Example Interaction Flow (Initial Synchronous Checkout):**

1.  **`gateway-bff`** receives `/checkout` request from frontend.
2.  **`gateway-bff`** calls **`checkout-orchestrator`** `/checkout` endpoint.
3.  **`checkout-orchestrator`** (Python) performs sequence of synchronous calls:
    - Calls **`cart-crud`** (TypeScript) to get cart details.
    - Calls **`inventory-read`** (Rust) to check stock.
    - Calls **`payment-gateway`** (Java) to process payment.
    - Calls **`order-create`** (Java) to create order.
    - Calls **`cart-crud`** (TypeScript) to clear cart.
    - Calls **`email-service`** (Go) to send confirmation.
4.  **`checkout-orchestrator`** returns success/failure to **`gateway-bff`**.
5.  **`gateway-bff`** returns response to frontend.

---

This roadmap provides a solid foundation for building the application incrementally. Once this initial version is stable, you can then proceed with the performance improvements and event-driven refactoring outlined in the `NextStep.md` file.

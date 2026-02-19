# Implementation Plan: Docker Setup and Refinement

## Phase 1: Repository Analysis and Dockerfile Strategy [checkpoint: 48a9715]

- [x] Task: Analyze existing services and their dependencies for Dockerization.
  - [x] Identify all services explicitly mentioned in `infra/docker/docker-compose.dev.yml`.
  - [x] For each identified service, determine its primary programming language, framework, and typical build process.
  - [x] Investigate any existing Dockerfiles or Docker Compose configurations within the project to understand current patterns and conventions.
  - [x] Research and consolidate modern Dockerfile best practices for Node.js and Rust, specifically focusing on production-ready images (multi-stage builds, minimal base images, security, efficient caching).
- [x] Task: Define a comprehensive Dockerfile strategy for both development and production environments.
  - [x] Outline a common pattern or template for creating robust, production-ready Docker images for each identified language/framework.
  - [x] Plan for consistent handling of environment variables, potential secrets (without hardcoding), and standardized logging mechanisms across services.
- [ ] Task: Conductor - User Manual Verification 'Repository Analysis and Dockerfile Strategy' (Protocol in workflow.md)

## Phase 2: Node.js Services Docker Implementation (`cart-crud` and `gateway-bff`)

- [ ] Task: Implement/Refine Dockerfile and Docker Compose configuration for `cart-crud`.
  - [ ] Analyze the `cart-crud` service's `package.json` and build scripts to understand its dependency management and build process.
  - [ ] Create a new `Dockerfile` for `cart-crud` following multi-stage build best practices, using a minimal Node.js base image, and installing production dependencies only.
  - [ ] Update `infra/docker/docker-compose.dev.yml` to utilize the newly created `Dockerfile` for `cart-crud`.
  - [ ] Implement a basic health check for the `cart-crud` service within its `Dockerfile` or `docker-compose.dev.yml`.
  - [ ] Verify that `docker compose build cart-crud` and `docker compose up cart-crud` successfully build and run the service.
  - [ ] Inspect logs for `cart-crud` to confirm successful startup and no critical errors.
- [ ] Task: Implement/Refine Dockerfile and Docker Compose configuration for `gateway-bff`.
  - [ ] Analyze the `gateway-bff` service's `package.json` and build scripts to understand its dependency management and build process.
  - [ ] Create a new `Dockerfile` for `gateway-bff` following multi-stage build best practices, using a minimal Node.js base image, and installing production dependencies only.
  - [ ] Update `infra/docker/docker-compose.dev.yml` to utilize the newly created `Dockerfile` for `gateway-bff`.
  - [ ] Implement a basic health check for the `gateway-bff` service within its `Dockerfile` or `docker-compose.dev.yml`.
  - [ ] Verify that `docker compose build gateway-bff` and `docker compose up gateway-bff` successfully build and run the service.
  - [ ] Inspect logs for `gateway-bff` to confirm successful startup and no critical errors.
- [ ] Task: Conductor - User Manual Verification 'Node.js Services Docker Implementation' (Protocol in workflow.md)

<h2>Phase 3: Rust Services Docker Refinement</h2>

- [ ] Task: Analyze and address brittleness and `sqlx` integration issues in Rust Docker setup.
  - [ ] Identify specific Rust services present in `infra/docker/docker-compose.dev.yml`.
  - [ ] Review existing Rust Dockerfiles (if any) and identify potential causes for brittleness or `sqlx` related failures.
  - [ ] Research and incorporate Rust Docker best practices, focusing on build caching (e.g., `cargo-chef`, BuildKit cache mounts) and robust `sqlx` database connection handling within a containerized environment.
- [ ] Task: Refine Dockerfile(s) for Rust services based on analysis.
  - [ ] Implement multi-stage builds for Rust services, compiling in an initial stage and copying the binary to a minimal runtime image (e.g., `scratch`, Alpine).
  - [ ] Integrate build caching mechanisms (e.g., `cargo-chef`) to optimize build times for Rust services.
  - [ ] Ensure `sqlx` environment variables and dependencies are correctly handled within the Docker environment for stable database connectivity.
  - [ ] Update `infra/docker/docker-compose.dev.yml` to reflect changes in Rust service Dockerfiles.
  - [ ] Verify that `docker compose build <rust_service>` and `docker compose up <rust_service>` successfully build and run the services.
  - [ ] Inspect logs for Rust services to confirm successful startup and `sqlx` functionality.
- [ ] Task: Conductor - User Manual Verification 'Rust Services Docker Refinement' (Protocol in workflow.md)

<h2>Phase 4: Other Services Docker Implementation/Verification</h2>

- [ ] Task: Iterate and implement/verify Docker configurations for all remaining services.
  - [ ] For each service not covered in Phase 2 or 3 but present in `infra/docker/docker-compose.dev.yml`, analyze its requirements.
  - [ ] If a Dockerfile is missing or suboptimal, create/refine it following best practices for its respective language/framework (e.g., Java/Spring Boot, Python/FastAPI).
  - [ ] Update `infra/docker/docker-compose.dev.yml` to use the appropriate Dockerfile for each service.
  - [ ] Implement basic health checks where appropriate for these services.
  - [ ] Verify that `docker compose build <service_name>` and `docker compose up <service_name>` successfully build and run each service.
  - [ ] Inspect logs for each service to confirm successful startup and no critical errors.
- [ ] Task: Conductor - User Manual Verification 'Other Services Docker Implementation/Verification' (Protocol in workflow.md)

<h2>Phase 5: Overall Verification and Cleanup</h2>

- [ ] Task: Perform comprehensive end-to-end verification of the entire Docker Compose setup.
  - [ ] Execute `docker compose build --no-cache --remove-orphans` to ensure a clean rebuild of all services.
  - [ ] Execute `docker compose up -d` to start all services in detached mode.
  - [ ] Monitor and inspect logs for all services (`docker compose logs --follow`) to confirm correct and stable operation.
  - [ ] Run basic connectivity tests between services (if applicable and easily testable) to ensure inter-service communication is functional.
- [ ] Task: Update project documentation regarding Docker setup and usage.
  - [ ] Add instructions for building and running the Docker Compose setup.
  - [ ] Document any specific considerations for individual services or development workflows.
- [ ] Task: Conductor - User Manual Verification 'Overall Verification and Cleanup' (Protocol in workflow.md)

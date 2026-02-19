# Tech Stack: Community Platform

This document outlines the core technologies and tools used within the Community Platform project.

## Programming Languages

- **TypeScript:** Primarily used for frontend applications and Backend-For-Frontend (BFF).
- **Python:** Utilized for various backend microservices.
- **Java:** Employed for specific backend microservices, often leveraging Spring Boot.
- **Rust:** Used for performance-critical backend microservices.

## Frameworks

- **Frontend Framework:**
  - **Next.js:** A React framework for building the Backend-For-Frontend (BFF) application, enabling server-side rendering and static site generation.
- **Backend Frameworks:**
  - **Spring Boot (Java):** A popular framework for building robust, production-ready Java applications.
  - **FastAPI (Python):** A modern, fast (high-performance) web framework for building APIs with Python 3.7+ based on standard Python type hints.
  - **Node.js/Express.js (TypeScript):** Used for building scalable network applications.
  - **Rust (actix-web, rocket, or similar):** Frameworks for building high-performance web services in Rust.

## Data Storage

- **Postgres:** A powerful, open-source relational database system used for core data persistence needs.

## Messaging & Event Streaming

- **Apache Kafka:** A distributed streaming platform used for building real-time data pipelines and streaming applications, facilitating asynchronous communication between microservices.

## Containerization & Orchestration

- **Docker & Docker Compose:** Used for packaging applications into containers and orchestrating multi-container Docker applications, simplifying local development environment setup.

## Package Management

- **Maven (Java):** The primary build automation tool used primarily for Java projects.
- **pnpm (Node.js/TypeScript):** A fast, disk space efficient package manager used for JavaScript/TypeScript projects within the monorepo.
- **Poetry (Python):** A dependency management and packaging tool for Python.
- **Cargo (Rust):** The Rust package manager and build system.

## Other Tools

- **uvicorn:** An ASGI web server implementation for Python, commonly used with FastAPI.

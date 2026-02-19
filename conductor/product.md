# Product Definition: Community Platform

## Initial Concept

This repository contains the foundational microservices and frontend applications for the Community Platform project. It's designed as a polyglot, event-driven microservices architecture leveraging a saga pattern for distributed transactions.

## Purpose and Goals

The Community Platform is an e-commerce platform built with multiple programming languages. Its primary purpose is to emulate a large, real-world production codebase, serving as a comprehensive example of a polyglot, event-driven microservices architecture. A unique aspect of the platform is its ability to communicate with the application via AI.

## Architecture

The platform utilizes a polyglot, event-driven microservices architecture, employing a saga pattern for distributed transactions. A Backend-For-Frontend (BFF) exposes its public API, coordinating with various backend microservices. Key architectural principles include:

- **Microservices:** Independent services for specific business capabilities.
- **Event-Driven Architecture (EDA):** Kafka for asynchronous communication and event streaming.
- **Saga Pattern:** Orchestration-based sagas (coordinated by `checkout-orchestrator`) for distributed transactions.
- **Polyglot Development:** Services are written in TypeScript, Python, Java, and Rust.

## Technology Stack

The platform leverages a diverse set of technologies to demonstrate a real-world, complex system:

- **Programming Languages:** TypeScript, Python, Java, Rust
- **Frontend Framework:** Next.js (for the Backend-For-Frontend)
- **Backend Frameworks:** Spring Boot (Java), FastAPI (Python), Node.js/Express.js (TypeScript), Rust (actix-web, rocket, or similar, inferred from Rust microservices)
- **Database:** Postgres (used for core infrastructure)
- **Messaging:** Apache Kafka (for asynchronous communication and event streaming)
- **Containerization:** Docker & Docker Compose (for running core infrastructure)
- **Package Management:** Maven (Java), pnpm (Node.js/TypeScript), Poetry (Python), Cargo (Rust)
- **Other Tools:** uvicorn (ASGI server for Python)

# Community Platform (Phase 1: Starter)

This repository contains the initial scaffolding for the Community Platform project. It includes a frontend BFF, two backend microservices, and the necessary infrastructure to run them locally.

## Prerequisites

- Docker & Docker Compose
- Java 17+ & Maven
- Node.js 20+ & npm

## Local Development

Follow these steps to get the environment running on your local machine.

### 1. Start Infrastructure

The core infrastructure (Postgres, Redis, Kafka) is managed with Docker Compose.

```sh
docker-compose -f infra/docker/docker-compose.dev.yml up -d
```

You can check the status of the containers with `docker ps`.

### 2. Run Backend Services

Each Java microservice can be run from its directory using the Spring Boot Maven plugin. It's recommended to run each command in a separate terminal.

**Product Write Service:**

```sh
cd services/catalog/product-write
mvn spring-boot:run
```

**Product Read Service:**

```sh
cd services/catalog/product-read
mvn spring-boot:run
```

### 3. Run Frontend Application

The Next.js BFF can be run using npm.

```sh
cd apps/gateway-bff
npm install
npm run dev
```

Once running, the application will be available at [http://localhost:3000](http://localhost:3000).

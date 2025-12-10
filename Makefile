PROTO_DIR := shared/proto

.PHONY: all-proto
all-proto: generate-proto-python generate-proto-go generate-proto-typescript

.PHONY: generate-proto-python
generate-proto-python:
	@echo "Generating Python Protobuf code..."
	./scripts/generate_proto.sh python services/inventory/warehouse-sync/warehouse_sync product.proto
	./scripts/generate_proto.sh python services/checkout/checkout-orchestrator/checkout_orchestrator product.proto
	./scripts/generate_proto.sh python services/checkout/checkout-orchestrator/checkout_orchestrator catalog_events.proto
	./scripts/generate_proto.sh python services/search/rec-model-service/rec_model_service product.proto
	./scripts/generate_proto.sh python services/ai/intent-parser/intent_parser product.proto
	./scripts/generate_proto.sh python services/ai/plan-generator/plan_generator product.proto
	./scripts/generate_proto.sh python services/ai/plan-executor/plan-executor product.proto

.PHONY: generate-proto-go
generate-proto-go:
	@echo "Generating Go Protobuf code..."
	./scripts/generate_proto.sh go services/notifications/email-service product.proto
	./scripts/generate_proto.sh go services/notifications/email-service catalog_events.proto
	./scripts/generate_proto.sh go services/notifications/sms-service product.proto
	./scripts/generate_proto.sh go services/notifications/push-service product.proto
	./scripts/generate_proto.sh go services/ops/metrics product.proto
	./scripts/generate_proto.sh go services/ops/tracing product.proto
	./scripts/generate_proto.sh go services/ops/log-forwarder product.proto
	./scripts/generate_proto.sh go services/ops/config product.proto

.PHONY: generate-proto-typescript
generate-proto-typescript:
	@echo "Generating TypeScript Protobuf code..."
	./scripts/generate_proto.sh typescript shared/libs/ts product.proto
	./scripts/generate_proto.sh typescript shared/libs/ts catalog_events.proto
	./scripts/generate_proto.sh typescript apps/gateway-bff/src/proto product.proto
	./scripts/generate_proto.sh typescript services/cart/cart-crud/src/proto product.proto

.PHONY: infra-up
infra-up:
	@echo "Starting local infrastructure..."
	docker-compose -f infra/docker/docker-compose.dev.yml up -d

.PHONY: infra-down
infra-down:
	@echo "Stopping local infrastructure..."
	docker-compose -f infra/docker/docker-compose.dev.yml down

.PHONY: dev
dev: infra-up
	@echo "Starting all services in development mode..."
	# Add commands to start individual services here
	# Example for Java: mvn spring-boot:run
	# Example for Python: poetry run uvicorn main:app --reload
	# Example for Node.js: pnpm dev
	@echo "Individual service startup commands need to be added here."

.PHONY: clean
clean: infra-down
	@echo "Cleaning up..."
	docker volume rm infra_docker_postgres_data infra_docker_redis_data infra_docker_prometheus_data infra_docker_grafana_data infra_docker_minio_data || true
	# Add clean commands for each service type (e.g., mvn clean, rm -rf node_modules, rm -rf target)

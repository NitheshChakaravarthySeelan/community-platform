.PHONY: dev-up dev-down fmt

dev-up:
 docker compose -f infra/docker/docker-compose.dev.yml up -d

dev-down:
 docker compose -f infra/docker/docker-compose.dev.yml down

fmt:
 @echo "Run code formatters per-language (not implemented by Makefile)"

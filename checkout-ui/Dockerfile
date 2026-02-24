# Stage 0: Base image for pnpm
FROM node:20-slim AS base
RUN npm install -g pnpm
ENV PNPM_HOME="/usr/local/bin"
ENV PATH="$PNPM_HOME:$PATH"
WORKDIR /repo

# Stage 1: Dependency Installation
FROM base AS deps
ENV GRPC_NODE_BUILD_FROM_SOURCE=true
WORKDIR /repo
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
# Next.js standalone build needs the package.json of the app and its dependencies
COPY checkout-ui/package.json checkout-ui/
COPY apps/*/package.json apps/
COPY services/*/package.json services/
COPY shared/libs/*/package.json shared/libs/
RUN pnpm install --frozen-lockfile

# Stage 2: Builder
FROM base AS builder
WORKDIR /repo
COPY --from=deps /repo/node_modules ./node_modules
COPY . .

# Build the specific service (checkout-ui)
ENV CI=true
ENV NEXT_FONT_GOOGLE_MOCK=1
ENV NEXT_OTEL_FETCH_DISABLED=1
# Re-run install for the filter to ensure symlinks are correct in this stage
RUN pnpm install --filter checkout-ui --offline --ignore-scripts
RUN pnpm --filter checkout-ui run build

# Stage 3: Runner
FROM base AS runner
WORKDIR /app

# Explicitly copy passwd and group from base stage to ensure user exists
COPY --from=base /etc/passwd /etc/passwd
COPY --from=base /etc/group /etc/group

ENV NODE_ENV production
ENV PORT 3000

# Copy the standalone output from the builder stage
COPY --from=builder --chown=node:node /repo/checkout-ui/.next/standalone/. /app

# Copy public assets and static assets
# In standalone mode, server.js is at /app/checkout-ui/server.js
# Next.js looks for public and static in two places depending on version/config:
# 1. Next to server.js
# 2. At the root of the standalone folder
COPY --from=builder --chown=node:node /repo/checkout-ui/public /app/checkout-ui/public
COPY --from=builder --chown=node:node /repo/checkout-ui/.next/static /app/checkout-ui/.next/static
COPY --from=builder --chown=node:node /repo/checkout-ui/public /app/public
COPY --from=builder --chown=node:node /repo/checkout-ui/.next/static /app/.next/static

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD curl -f http://localhost:3000/ || exit 1

EXPOSE 3000

CMD ["su", "-c", "node /app/checkout-ui/server.js", "node"]

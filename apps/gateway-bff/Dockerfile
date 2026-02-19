# Stage 0: Base image for pnpm
FROM node:20-slim AS base
RUN npm install -g pnpm
ENV PNPM_HOME="/usr/local/bin"
ENV PATH="$PNPM_HOME:$PATH"
WORKDIR /repo
# Using default 'node' non-root user from base image

# Stage 1: Dependency Installation (monorepo wide)
FROM base AS deps
WORKDIR /repo
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY apps/*/package.json apps/
COPY services/*/package.json services/
COPY shared/libs/*/package.json shared/libs/
RUN pnpm install --frozen-lockfile

# Stage 2: Builder
FROM base AS builder
WORKDIR /repo
# Copy installed dependencies from deps stage
COPY --from=deps /repo/node_modules ./node_modules
# Copy the entire monorepo context
COPY . .

# Tell pnpm it's running in a CI/non-interactive environment
ENV CI=true

# Copy shared proto files to gateway-bff's expected location for build
RUN cp -R shared/proto apps/gateway-bff/proto

# Build the specific service (gateway-bff)
# Install service-specific dependencies (including dev for proto:gen and build)
RUN pnpm install --filter gateway-bff --no-dev --ignore-scripts
# Run proto generation for gateway-bff
RUN pnpm --filter gateway-bff run proto:gen
# Then build the service
RUN pnpm --filter gateway-bff run build

# Stage 3: Runner
FROM base AS runner
WORKDIR /app

# Explicitly copy passwd and group from base stage to ensure user exists
COPY --from=base /etc/passwd /etc/passwd
COPY --from=base /etc/group /etc/group

ENV NODE_ENV production
ENV PORT 3004 # Keep original port

# Copy the standalone output from the builder stage, chown to 'node' user
COPY --from=builder --chown=node:node /repo/apps/gateway-bff/.next/standalone/. /app

# Copy public assets and static assets (these are separate from standalone output usually)
COPY --from=builder --chown=node:node /repo/apps/gateway-bff/public/. /app/public
COPY --from=builder --chown=node:node /repo/apps/gateway-bff/.next/static/. /app/.next/static

HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD curl -f http://localhost:3000/ || exit 1 # Health check to root path (internal port)

EXPOSE 3004

# Explicitly run as 'node' user using su -c and full path to server.js
CMD ["su", "-c", "node /app/apps/gateway-bff/server.js", "node"]

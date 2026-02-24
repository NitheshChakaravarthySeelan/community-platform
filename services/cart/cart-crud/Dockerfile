# Stage 0: Base image for pnpm
FROM node:20-slim AS base
RUN npm install -g pnpm
ENV PNPM_HOME="/usr/local/bin"
ENV PATH="$PNPM_HOME:$PATH"
WORKDIR /repo
# Using default 'node' non-root user from base image

# Stage 1: Dependency Installation (monorepo wide)
FROM base AS deps
ENV GRPC_NODE_BUILD_FROM_SOURCE=true
WORKDIR /repo
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY services/cart/cart-crud/package.json services/cart/cart-crud/
# Install all dependencies including devDependencies for the build stage
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

# Build the specific service (cart-crud)
# Ensure all commands are run from the monorepo root with filtering where applicable
WORKDIR /repo
# Install service-specific dependencies (including dev for proto:gen and build)
RUN pnpm install --filter cart-crud --no-dev --ignore-scripts
# Run proto generation for cart-crud
RUN pnpm --filter cart-crud run proto:gen
# Then build the service
RUN pnpm --filter cart-crud run build

# After building, re-install dependencies to get only production ones for the runner image
WORKDIR /repo/services/cart/cart-crud
RUN pnpm install --prod --no-frozen-lockfile --ignore-scripts

# Stage 3: Runner
FROM base AS runner
WORKDIR /app

# Explicitly copy passwd and group from base stage to ensure user exists
COPY --from=base /etc/passwd /etc/passwd
COPY --from=base /etc/group /etc/group

ENV NODE_ENV production
ENV PORT 3000

# Copy built application output, chown to 'node' user
COPY --from=builder --chown=node:node /repo/services/cart/cart-crud/dist ./dist

# Copy package.json and pnpm-lock.yaml from builder stage, chown to 'node' user
COPY --from=builder --chown=node:node /repo/services/cart/cart-crud/package.json ./package.json
COPY --from=builder --chown=node:node /repo/pnpm-lock.yaml ./pnpm-lock.yaml

# Install production dependencies as root (temporarily)
# This circumvents permission issues with pnpm's store
RUN pnpm install --prod --no-frozen-lockfile --ignore-scripts

HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD curl -f http://localhost:3000/ || exit 1
EXPOSE 3000

# Explicitly run as 'node' user using su -c
CMD ["su", "-c", "node dist/index.js", "node"]

# Stage 1: Install dependencies (monorepo root context)
FROM node:20-alpine AS deps
WORKDIR /app

# Copy essential monorepo files for dependency resolution
COPY package.json pnpm-lock.yaml turbo.json ./
# Copy specific cart-crud files needed for pnpm install to understand dependencies
# This is crucial for pnpm to correctly link workspace dependencies
COPY services/cart/cart-crud/package.json ./services/cart/cart-crud/
COPY services/cart/cart-crud/tsconfig.json ./services/cart/cart-crud/

# Run pnpm install at the monorepo root
RUN npm install -g pnpm
ENV CI=true
RUN pnpm install --frozen-lockfile

# Stage 2: Build the application (for cart-crud)
FROM node:20-alpine AS builder
WORKDIR /app

# Copy necessary files from deps stage: node_modules and cart-crud source
COPY --from=deps /app/node_modules ./node_modules
COPY services/cart/cart-crud ./services/cart/cart-crud

# Copy the entire monorepo source (excluding node_modules) for build if needed
# This will be copied from the build context (monorepo root)
COPY . .

RUN npm install -g pnpm
ENV PATH="/usr/local/bin:$PATH"

WORKDIR /app/services/cart/cart-crud
RUN pnpm build

# Stage 3: Create the final, lightweight production container
FROM node:20-alpine AS runner
WORKDIR /app/services/cart/cart-crud

ENV NODE_ENV production

# Copy cart-crud's package.json and the monorepo's pnpm-lock.yaml directly to its WORKDIR
COPY services/cart/cart-crud/package.json ./package.json
COPY pnpm-lock.yaml ./pnpm-lock.yaml

# Install pnpm globally within this stage
RUN npm install -g pnpm

# Install only production dependencies for cart-crud in its own directory
RUN pnpm install --prod

# Copy build output for cart-crud
COPY --from=builder /app/services/cart/cart-crud/dist ./dist

USER node

EXPOSE 3000
ENV PORT 3000

CMD ["node", "dist/index.js"]
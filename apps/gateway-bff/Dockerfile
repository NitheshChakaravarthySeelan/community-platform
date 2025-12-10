# Stage 1: Install dependencies
FROM node:20-alpine AS deps
WORKDIR /app
COPY . .
RUN npm install -g pnpm
ENV CI=true
RUN pnpm install --frozen-lockfile

# Stage 2: Build the application
FROM node:20-alpine AS builder
WORKDIR /app/apps/gateway-bff
RUN npm install -g pnpm
COPY --from=deps /app ./
ENV NEXT_TELEMETRY_DISABLED 1
RUN pnpm build

# Stage 3: Create the final, lightweight production container
FROM node:20-alpine AS runner
WORKDIR /app/apps/gateway-bff

ENV NODE_ENV production
ENV NEXT_TELEMETRY_DISABLED 1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/apps/gateway-bff/public ./public
COPY --from=builder /app/apps/gateway-bff/.next/standalone ./ # This copies the standalone output
COPY --from=builder /app/apps/gateway-bff/.next/static ./.next/static

USER nextjs

EXPOSE 3000
ENV PORT 3000

CMD ["node", "server.js"]
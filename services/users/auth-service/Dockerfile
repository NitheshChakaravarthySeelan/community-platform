# Stage 1: Install dependencies
FROM node:20-alpine AS deps
WORKDIR /app
COPY . .
RUN npm install -g pnpm
ENV CI=true
RUN pnpm install --frozen-lockfile

# Stage 2: Build the application
FROM node:20-alpine AS builder
WORKDIR /app/services/users/auth-service
RUN npm install -g pnpm
COPY --from=deps /app ./
RUN pnpm build

# Stage 3: Create the final, lightweight production container
FROM node:20-alpine AS runner
WORKDIR /app/services/users/auth-service

ENV NODE_ENV production

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/services/users/auth-service/node_modules ./node_modules
COPY --from=builder /app/services/users/auth-service/dist ./dist

USER nextjs

EXPOSE 3002
ENV PORT 3002

CMD ["node", "dist/index.js"]
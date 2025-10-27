FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json pnpm-lock.yaml ./ # Assuming pnpm-lock.yaml exists
RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm run build # Assuming a build script for TypeScript compilation

FROM node:20-alpine

WORKDIR /app

COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/dist ./dist # Assuming TypeScript output to dist

EXPOSE 3000 # Or whatever port Express listens on

CMD ["node", "dist/index.js"] # Assuming compiled entry point
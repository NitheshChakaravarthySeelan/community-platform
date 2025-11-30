# Stage 1: Build the application
FROM node:18-alpine AS build
WORKDIR /app

# Copy package management files
COPY package.json pnpm-lock.yaml ./

# Install pnpm and dependencies
RUN npm install -g pnpm
RUN pnpm install

# Copy the rest of the source code
COPY . .

# Build the TypeScript code
RUN pnpm build

# Stage 2: Create the final, lightweight production container
FROM node:18-alpine
WORKDIR /app

# Copy dependencies and built code from the 'build' stage
COPY --from=build /app/node_modules ./node_modules
COPY --from=build /app/dist ./dist

# Expose the port the app runs on
EXPOSE 3002

# Command to run the application
CMD ["node", "dist/index.js"]

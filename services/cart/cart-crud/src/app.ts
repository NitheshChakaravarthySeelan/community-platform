import express, { type Request, type Response } from "express"; // Use type imports
import { createCartRoutes } from "./routes/cart.routes";
import * as client from "prom-client";
import { CartService } from "./services/cart.service";
import { PostgresCartRepository } from "./repositories/PostgresCartRepository";
import { ProductServiceAdapter } from "./adapters/ProductServiceAdapter";

const app: express.Application = express(); // Explicitly type app

app.use(express.json());

app.get("/", (req: Request, res: Response) => {
  res.send("Cart CRUD Service is running!");
});

// Prometheus Metrics Endpoint
app.get("/metrics", async (req: Request, res: Response) => {
  res.set("Content-Type", client.register.contentType);
  res.end(await client.register.metrics());
});

// Register default metrics
client.collectDefaultMetrics();

// Instantiate dependencies
const cartRepository = new PostgresCartRepository();
const productServiceAdapter = new ProductServiceAdapter();
export const cartService = new CartService(
  cartRepository,
  productServiceAdapter,
);

// Use the createCartRoutes function to get the router
app.use("/api/v1/carts", createCartRoutes(cartService));

export default app;

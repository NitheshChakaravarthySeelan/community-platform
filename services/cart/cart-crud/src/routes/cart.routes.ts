import { Router } from "express"; // Import Router as value
import type { Request, Response } from "express"; // Import Request and Response as types
import { body, param } from "express-validator";
import { validateRequest } from "../utils/validator.js";
import type { CartService } from "../services/cart.service.js"; // Import CartService as type

// Export a function that creates the router, allowing for dependency injection
export const createCartRoutes = (cartService: CartService): Router => {
  const router = Router();

  router.get(
    "/:userId",
    param("userId").isInt().toInt(),
    validateRequest,
    async (req: Request, res: Response) => {
      try {
        const cart = await cartService.getCart(Number(req.params.userId));
        if (!cart) {
          return res
            .status(404)
            .json({ message: "Cart not found for this user." });
        }
        res.status(200).json(cart);
      } catch (error) {
        res.status(500).json({ message: "Internal Server Error" });
      }
    },
  );

  router.post(
    "/:userId/items",
    param("userId").isInt().toInt(),
    body("productId").isInt().toInt(),
    body("quantity").isInt({ gt: 0 }).toInt(),
    validateRequest,
    async (req: Request, res: Response) => {
      const { userId } = req.params;
      const { productId, quantity } = req.body;
      try {
        const updatedCart = await cartService.addItem(
          Number(userId),
          productId,
          quantity,
        );
        res.status(200).json(updatedCart);
      } catch (error) {
        res.status(500).json({ message: "Internal Server Error" });
      }
    },
  );

  router.put(
    "/:userId/items/:productId",
    param("userId").isInt().toInt(),
    param("productId").isInt().toInt(),
    body("quantity").isInt().toInt(),
    validateRequest,
    async (req: Request, res: Response) => {
      const { userId, productId } = req.params;
      const { quantity } = req.body;
      try {
        const updatedCart = await cartService.updateItemQuantity(
          Number(userId),
          Number(productId),
          quantity,
        );
        res.status(200).json(updatedCart);
      } catch (error) {
        res.status(404).json({ message: (error as Error).message });
      }
    },
  );

  router.delete(
    "/:userId/items/:productId",
    param("userId").isInt().toInt(),
    param("productId").isInt().toInt(),
    validateRequest,
    async (req: Request, res: Response) => {
      const { userId, productId } = req.params;
      try {
        const updatedCart = await cartService.removeItem(
          Number(userId),
          Number(productId),
        );
        res.status(200).json(updatedCart);
      } catch (error) {
        res.status(404).json({ message: (error as Error).message });
      }
    },
  );

  return router;
};

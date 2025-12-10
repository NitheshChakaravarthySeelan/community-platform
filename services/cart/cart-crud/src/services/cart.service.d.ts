import type { Cart, CartDetails } from "../models/cart";
import type { CartRepository } from "../repositories/CartRepository";
import type { ProductServiceAdapter } from "../adapters/ProductServiceAdapter";
export declare class CartService {
  private cartRepository;
  private productServiceAdapter;
  constructor(
    cartRepository: CartRepository,
    productServiceAdapter: ProductServiceAdapter,
  );
  getOrCreateCart(userId: number): Promise<Cart>;
  addItem(userId: number, productId: number, quantity: number): Promise<Cart>;
  getCart(userId: number): Promise<CartDetails | null>;
  updateItemQuantity(
    userId: number,
    productId: number,
    quantity: number,
  ): Promise<Cart>;
  removeItem(userId: number, productId: number): Promise<Cart>;
}
//# sourceMappingURL=cart.service.d.ts.map

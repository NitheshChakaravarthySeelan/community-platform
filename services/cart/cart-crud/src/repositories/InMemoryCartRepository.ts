import type { Cart } from "../models/cart";
import type { CartRepository } from "./CartRepository";

export class InMemoryCartRepository implements CartRepository {
  private carts: Map<number, Cart> = new Map();
  private nextId = 1;

  async findByUserId(userId: number): Promise<Cart | null> {
    for (const cart of this.carts.values()) {
      if (cart.userId === userId) {
        return cart;
      }
    }
    return null;
  }

  async save(cart: Omit<Cart, "id"> | Cart): Promise<Cart> {
    if ("id" in cart && cart.id) {
      this.carts.set(cart.id, cart as Cart);
      return cart as Cart;
    } else {
      const newCart = { ...cart, id: this.nextId++ } as Cart;
      this.carts.set(newCart.id, newCart);
      return newCart;
    }
  }
}

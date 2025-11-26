import { Cart } from "../models/cart";

export interface CartRepository {
  findByUserId(userId: number): Promise<Cart | null>;
  save(cart: Omit<Cart, "id"> | Cart): Promise<Cart>;
}

import type { Cart } from "../models/cart";
import type { CartRepository } from "./CartRepository";
export declare class PostgresCartRepository implements CartRepository {
  findByUserId(userId: number): Promise<Cart | null>;
  save(cart: Omit<Cart, "id"> | Cart): Promise<Cart>;
}
//# sourceMappingURL=PostgresCartRepository.d.ts.map

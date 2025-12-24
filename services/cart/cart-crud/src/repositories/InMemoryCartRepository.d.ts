import type { Cart } from "../models/cart";
import type { CartRepository } from "./CartRepository";
export declare class InMemoryCartRepository implements CartRepository {
    private carts;
    private nextId;
    findByUserId(userId: number): Promise<Cart | null>;
    save(cart: Omit<Cart, "id"> | Cart): Promise<Cart>;
}
//# sourceMappingURL=InMemoryCartRepository.d.ts.map
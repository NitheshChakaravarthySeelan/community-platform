export class InMemoryCartRepository {
    carts = new Map();
    nextId = 1;
    async findByUserId(userId) {
        for (const cart of this.carts.values()) {
            if (cart.userId === userId) {
                return cart;
            }
        }
        return null;
    }
    async save(cart) {
        if ("id" in cart && cart.id) {
            this.carts.set(cart.id, cart);
            return cart;
        }
        else {
            const newCart = { ...cart, id: this.nextId++ };
            this.carts.set(newCart.id, newCart);
            return newCart;
        }
    }
}
//# sourceMappingURL=InMemoryCartRepository.js.map
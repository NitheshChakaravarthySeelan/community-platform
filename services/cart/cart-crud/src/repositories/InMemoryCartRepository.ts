const carts = [];
let nextId = 1;

class InMemoryCartRepository {
  async findByUserId(userId) {
    const cart = carts.find((c) => c.userId === userId);
    return cart ? { ...cart } : null;
  }

  async save(cart) {
    const existingIndex = carts.findIndex((c) => c.id === cart.id);

    if (existingIndex > -1) {
      carts[existingIndex] = { ...cart };
      return { ...carts[existingIndex] };
    } else {
      const newCart = { ...cart, id: nextId++ };
      carts.push(newCart);
      return { ...newCart };
    }
  }

  async clear() {
    carts.length = 0;
    nextId = 1;
  }
}

module.exports = { InMemoryCartRepository };

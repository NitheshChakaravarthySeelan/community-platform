class CartService {
  constructor(cartRepository) {
    this.cartRepository = cartRepository;
  }

  async getOrCreateCart(userId) {
    const existingCart = await this.cartRepository.findByUserId(userId);
    if (existingCart) {
      return existingCart;
    }

    const newCart = {
      userId,
      items: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    };
    return this.cartRepository.save(newCart);
  }

  async addItem(userId, productId, quantity) {
    if (quantity <= 0) {
      throw new Error("Quantity must be positive.");
    }
    const cart = await this.getOrCreateCart(userId);
    const existingItemIndex = cart.items.findIndex(
      (item) => item.productId === productId,
    );

    if (existingItemIndex > -1) {
      cart.items[existingItemIndex].quantity += quantity;
    } else {
      cart.items.push({
        id: Math.floor(Math.random() * 10000),
        productId,
        quantity,
      });
    }
    cart.updatedAt = new Date();
    return this.cartRepository.save(cart);
  }

  async getCart(userId) {
    return this.cartRepository.findByUserId(userId);
  }

  async updateItemQuantity(userId, productId, quantity) {
    if (quantity <= 0) {
      return this.removeItem(userId, productId);
    }
    const cart = await this.getOrCreateCart(userId);
    const itemIndex = cart.items.findIndex(
      (item) => item.productId === productId,
    );

    if (itemIndex === -1) {
      throw new Error("Item not found in cart.");
    }
    cart.items[itemIndex].quantity = quantity;
    cart.updatedAt = new Date();
    return this.cartRepository.save(cart);
  }

  async removeItem(userId, productId) {
    const cart = await this.getOrCreateCart(userId);
    const initialLength = cart.items.length;
    cart.items = cart.items.filter((item) => item.productId !== productId);

    if (cart.items.length === initialLength) {
      throw new Error("Item not found in cart.");
    }
    cart.updatedAt = new Date();
    return this.cartRepository.save(cart);
  }
}

module.exports = { CartService };

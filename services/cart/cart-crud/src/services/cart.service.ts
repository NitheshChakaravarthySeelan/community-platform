import { Cart, CartDetails } from "../models/cart";
import { CartRepository } from "../repositories/CartRepository";

export class CartService {
  constructor(private cartRepository: CartRepository) {}

  async getOrCreateCart(userId: number): Promise<Cart> {
    const existingCart = await this.cartRepository.findByUserId(userId);
    if (existingCart) {
      return existingCart;
    }

    const newCart: Omit<Cart, "id"> = {
      userId,
      items: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    };
    return this.cartRepository.save(newCart);
  }

  async addItem(
    userId: number,
    productId: number,
    quantity: number,
  ): Promise<Cart> {
    if (quantity <= 0) {
      throw new Error("Quantity must be positive.");
    }

    // TODO: Call product-read service to get product details
    // const product: Product = await productReadService.getProduct(productId);
    // if (!product) {
    //   throw new Error('Product not found.');
    // }

    const cart = await this.getOrCreateCart(userId);
    const existingItemIndex = cart.items.findIndex(
      (item) => item.productId === productId,
    );

    if (existingItemIndex > -1) {
      cart.items[existingItemIndex].quantity += quantity;
    } else {
      cart.items.push({
        id: Math.floor(Math.random() * 10000), // This should be handled by the database
        productId,
        quantity,
      });
    }
    cart.updatedAt = new Date();
    return this.cartRepository.save(cart);
  }

  async getCart(userId: number): Promise<CartDetails | null> {
    const cart = await this.cartRepository.findByUserId(userId);
    if (!cart) {
      return null;
    }

    // TODO: Call cart-pricing service to get the total price
    // const totalPrice = await cartPricingService.calculateTotalPrice(cart.items);

    // TODO: Enrich cart items with product details from product-read service
    const enrichedItems = await Promise.all(
      cart.items.map(async (item) => {
        // const product = await productReadService.getProduct(item.productId);
        return {
          ...item,
          name: "mock product name", // product.name,
          price: 10, // product.price,
          image: "mock-image.png", // product.image
        };
      }),
    );

    const cartDetails: CartDetails = {
      ...cart,
      items: enrichedItems,
      totalPrice: 100, // totalPrice,
    };

    return cartDetails;
  }

  async updateItemQuantity(
    userId: number,
    productId: number,
    quantity: number,
  ): Promise<Cart> {
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

  async removeItem(userId: number, productId: number): Promise<Cart> {
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

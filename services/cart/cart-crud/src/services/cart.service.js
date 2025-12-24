export class CartService {
    cartRepository;
    productServiceAdapter;
    constructor(cartRepository, productServiceAdapter) {
        this.cartRepository = cartRepository;
        this.productServiceAdapter = productServiceAdapter;
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
        const product = await this.productServiceAdapter.getProductById(productId);
        if (!product) {
            throw new Error("Product not found.");
        }
        const cart = await this.getOrCreateCart(userId);
        const existingItemIndex = cart.items.findIndex((item) => item.productId === productId);
        if (existingItemIndex > -1) {
            cart.items[existingItemIndex].quantity += quantity;
        }
        else {
            cart.items.push({
                id: Math.floor(Math.random() * 10000), // This should be handled by the database
                productId,
                quantity,
            });
        }
        cart.updatedAt = new Date();
        return this.cartRepository.save(cart);
    }
    async getCart(userId) {
        const cart = await this.cartRepository.findByUserId(userId);
        if (!cart) {
            return null;
        }
        // TODO: Call cart-pricing service to get the total price
        // const totalPrice = await cartPricingService.calculateTotalPrice(cart.items);
        const enrichedItems = await Promise.all(cart.items.map(async (item) => {
            const product = await this.productServiceAdapter.getProductById(item.productId);
            return {
                ...item,
                name: product?.name,
                price: product?.price,
                image: product?.imageUrl,
            };
        }));
        const cartDetails = {
            ...cart,
            items: enrichedItems,
            totalPrice: 100, // totalPrice,
        };
        return cartDetails;
    }
    async updateItemQuantity(userId, productId, quantity) {
        if (quantity <= 0) {
            return this.removeItem(userId, productId);
        }
        const cart = await this.getOrCreateCart(userId);
        const itemIndex = cart.items.findIndex((item) => item.productId === productId);
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
    async clearCartByUserId(userId) {
        // Assuming cartRepository.findByUserId expects a number, convert the string userId
        const cart = await this.cartRepository.findByUserId(Number(userId));
        if (!cart) {
            // It's possible a user might not have an active cart, or it was already cleared/deleted.
            // Depending on business rules, this might be an error or simply a no-op.
            // For now, let's just log and consider it done if no cart is found.
            console.warn(`No active cart found for user ID: ${userId} to clear.`);
            return;
        }
        cart.items = [];
        cart.updatedAt = new Date();
        await this.cartRepository.save(cart);
    }
}
//# sourceMappingURL=cart.service.js.map
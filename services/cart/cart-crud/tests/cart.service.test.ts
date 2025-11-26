import { CartService } from "../src/services/cart.service";
import { CartRepository } from "../src/repositories/CartRepository";
import { Cart, CartDetails, CartItem } from "../src/models/cart";

// Mock Implementation of CartRepository for testing purposes
const mockCartRepository: jest.Mocked<CartRepository> = {
  findByUserId: jest.fn(),
  save: jest.fn(),
};

// Clear all mocks before each test to ensure a clean slate
beforeEach(() => {
  jest.clearAllMocks();
});

describe("CartService", () => {
  let cartService: CartService;

  // Create a new instance of CartService with the mock repository before each test
  beforeEach(() => {
    cartService = new CartService(mockCartRepository);
  });

  // --- Tests for getCart ---
  describe("getCart", () => {
    it("should return cart details if a cart is found", async () => {
      // Arrange
      const userId = 1;
      const mockCart: Cart = {
        id: 1,
        userId,
        items: [{ id: 1, productId: 101, quantity: 2 }],
        createdAt: new Date(),
        updatedAt: new Date(),
      };
      mockCartRepository.findByUserId.mockResolvedValue(mockCart);

      // Act
      const result = (await cartService.getCart(userId)) as CartDetails;

      // Assert
      expect(result).not.toBeNull();
      expect(result.userId).toBe(userId);
      expect(result.items.length).toBe(1);
      expect(result.totalPrice).toBe(100); // Mocked value from the service
      expect(mockCartRepository.findByUserId).toHaveBeenCalledWith(userId);
    });

    it("should return null if no cart is found", async () => {
      // Arrange
      const userId = 99;
      mockCartRepository.findByUserId.mockResolvedValue(null);

      // Act
      const result = await cartService.getCart(userId);

      // Assert
      expect(result).toBeNull();
      expect(mockCartRepository.findByUserId).toHaveBeenCalledWith(userId);
    });
  });

  // --- Tests for addItem ---
  describe("addItem", () => {
    it("should add a new item to a cart that does not exist yet", async () => {
      // Arrange
      const userId = 2;
      const productId = 202;
      const quantity = 1;
      const newCart: Cart = {
        id: 2,
        userId,
        items: [{ id: 1, productId, quantity }],
        createdAt: new Date(),
        updatedAt: new Date(),
      };
      mockCartRepository.findByUserId.mockResolvedValue(null); // No existing cart
      mockCartRepository.save.mockResolvedValue(newCart); // The save operation will return the new cart

      // Act
      const result = await cartService.addItem(userId, productId, quantity);

      // Assert
      expect(mockCartRepository.findByUserId).toHaveBeenCalledWith(userId);
      expect(mockCartRepository.save).toHaveBeenCalled();
      expect(result.items.length).toBe(1);
      expect(result.items[0].productId).toBe(productId);
    });

    it("should increment the quantity of an existing item", async () => {
      // Arrange
      const userId = 1;
      const productId = 101;
      const initialQuantity = 2;
      const addedQuantity = 3;
      const existingCart: Cart = {
        id: 1,
        userId,
        items: [{ id: 1, productId, quantity: initialQuantity }],
        createdAt: new Date(),
        updatedAt: new Date(),
      };
      const updatedCart: Cart = {
        ...existingCart,
        items: [
          { id: 1, productId, quantity: initialQuantity + addedQuantity },
        ],
      };

      mockCartRepository.findByUserId.mockResolvedValue(existingCart);
      mockCartRepository.save.mockResolvedValue(updatedCart);

      // Act
      const result = await cartService.addItem(
        userId,
        productId,
        addedQuantity,
      );

      // Assert
      expect(mockCartRepository.save).toHaveBeenCalled();
      expect(result.items[0].quantity).toBe(initialQuantity + addedQuantity);
    });
  });

  // --- Tests for updateItemQuantity ---
  describe("updateItemQuantity", () => {
    it("should update the quantity of an item in the cart", async () => {
      // Arrange
      const userId = 1;
      const productId = 101;
      const newQuantity = 5;
      const existingCart: Cart = {
        id: 1,
        userId,
        items: [{ id: 1, productId, quantity: 2 }],
        createdAt: new Date(),
        updatedAt: new Date(),
      };
      const updatedCart: Cart = {
        ...existingCart,
        items: [{ id: 1, productId, quantity: newQuantity }],
      };

      mockCartRepository.findByUserId.mockResolvedValue(existingCart);
      mockCartRepository.save.mockResolvedValue(updatedCart);

      // Act
      const result = await cartService.updateItemQuantity(
        userId,
        productId,
        newQuantity,
      );

      // Assert
      expect(result.items[0].quantity).toBe(newQuantity);
      expect(mockCartRepository.save).toHaveBeenCalledWith(
        expect.objectContaining({
          items: expect.arrayContaining([
            expect.objectContaining({ quantity: 5 }),
          ]),
        }),
      );
    });

    it("should throw an error if the item is not found in the cart", async () => {
      // Arrange
      const userId = 1;
      const nonExistentProductId = 999;
      const existingCart: Cart = {
        id: 1,
        userId,
        items: [{ id: 1, productId: 101, quantity: 2 }],
        createdAt: new Date(),
        updatedAt: new Date(),
      };
      mockCartRepository.findByUserId.mockResolvedValue(existingCart);

      // Act & Assert
      await expect(
        cartService.updateItemQuantity(userId, nonExistentProductId, 5),
      ).rejects.toThrow("Item not found in cart.");
    });
  });

  // --- Tests for removeItem ---
  describe("removeItem", () => {
    it("should remove an item from the cart", async () => {
      // Arrange
      const userId = 1;
      const productIdToRemove = 101;
      const existingCart: Cart = {
        id: 1,
        userId,
        items: [
          { id: 1, productId: 101, quantity: 2 },
          { id: 2, productId: 102, quantity: 1 },
        ],
        createdAt: new Date(),
        updatedAt: new Date(),
      };
      const updatedCart: Cart = {
        ...existingCart,
        items: [{ id: 2, productId: 102, quantity: 1 }],
      };

      mockCartRepository.findByUserId.mockResolvedValue(existingCart);
      mockCartRepository.save.mockResolvedValue(updatedCart);

      // Act
      const result = await cartService.removeItem(userId, productIdToRemove);

      // Assert
      expect(result.items.length).toBe(1);
      expect(result.items[0].productId).not.toBe(productIdToRemove);
      expect(mockCartRepository.save).toHaveBeenCalled();
    });
  });
});

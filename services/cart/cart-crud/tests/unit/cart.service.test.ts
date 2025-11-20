const { CartService } = require("../../src/services/cart.service");
const {
  InMemoryCartRepository,
} = require("../../src/repositories/InMemoryCartRepository");

describe("CartService", () => {
  let cartService;
  let cartRepository;

  beforeEach(async () => {
    cartRepository = new InMemoryCartRepository();
    await cartRepository.clear();
    cartService = new CartService(cartRepository);
  });

  it("should add an item to a new cart", async () => {
    const userId = 1;
    const productId = 101;
    const quantity = 2;

    const cart = await cartService.addItem(userId, productId, quantity);

    expect(cart).toBeDefined();
    expect(cart.userId).toBe(userId);
    expect(cart.items.length).toBe(1);
    expect(cart.items[0].productId).toBe(productId);
    expect(cart.items[0].quantity).toBe(quantity);
  });

  it("should add quantity to an existing item in the cart", async () => {
    const userId = 1;
    await cartService.addItem(userId, 101, 1);
    const cart = await cartService.addItem(userId, 101, 2);

    expect(cart.items.length).toBe(1);
    expect(cart.items[0].quantity).toBe(3);
  });

  it("should update an items quantity", async () => {
    const userId = 1;
    await cartService.addItem(userId, 101, 1);
    const cart = await cartService.updateItemQuantity(userId, 101, 5);

    expect(cart.items[0].quantity).toBe(5);
  });

  it("should remove an item if quantity is updated to 0 or less", async () => {
    const userId = 1;
    await cartService.addItem(userId, 101, 1);
    await cartService.addItem(userId, 102, 1);

    const cart = await cartService.updateItemQuantity(userId, 101, 0);

    expect(cart.items.length).toBe(1);
    expect(cart.items[0].productId).toBe(102);
  });

  it("should remove an item from the cart", async () => {
    const userId = 1;
    await cartService.addItem(userId, 101, 1);
    await cartService.addItem(userId, 102, 2);

    const cart = await cartService.removeItem(userId, 101);

    expect(cart.items.length).toBe(1);
    expect(cart.items[0].productId).toBe(102);
  });

  it("should retrieve a user cart", async () => {
    const userId = 1;
    await cartService.addItem(userId, 101, 1);

    const cart = await cartService.getCart(userId);
    expect(cart).toBeDefined();
    expect(cart?.userId).toBe(userId);
    expect(cart?.items.length).toBe(1);
  });
});

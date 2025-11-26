import request from "supertest";
import app from "../../src/app";
import { PostgresCartRepository } from "../../src/repositories/PostgresCartRepository";
import { Cart } from "../../src/models/cart";

// --- Mocking the Repository ---
// This tells Jest to replace the real PostgresCartRepository with a mock version
// before any of our application code (in app.ts, routes, etc.) runs.
jest.mock("../../src/repositories/PostgresCartRepository");

const MockedPostgresCartRepository = PostgresCartRepository as jest.MockedClass<
  typeof PostgresCartRepository
>;

describe("Cart API", () => {
  const userId = 123;

  beforeEach(() => {
    // Clear all mock implementations before each test
    MockedPostgresCartRepository.mockClear();
  });

  it("should return a 404 for a non-existent cart", async () => {
    // Arrange: Tell the mock repository to return null when findByUserId is called
    MockedPostgresCartRepository.prototype.findByUserId.mockResolvedValue(null);

    // Act
    const res = await request(app).get(`/api/v1/carts/999`);

    // Assert
    expect(res.statusCode).toEqual(404);
  });

  it("should add an item to the cart", async () => {
    // Arrange
    const newItem = { productId: 101, quantity: 1 };
    const expectedCart: Cart = {
      id: 1,
      userId,
      items: [{ id: 1, ...newItem }],
      createdAt: new Date(),
      updatedAt: new Date(),
    };
    MockedPostgresCartRepository.prototype.findByUserId.mockResolvedValue(null); // No initial cart
    MockedPostgresCartRepository.prototype.save.mockResolvedValue(expectedCart); // Return the new cart on save

    // Act
    const res = await request(app)
      .post(`/api/v1/carts/${userId}/items`)
      .send(newItem);

    // Assert
    expect(res.statusCode).toEqual(200);
    expect(res.body.userId).toEqual(userId);
    expect(res.body.items.length).toBe(1);
    expect(res.body.items[0].productId).toBe(101);
  });

  it("should update an item quantity", async () => {
    // Arrange
    const update = { quantity: 3 };
    const existingCart: Cart = {
      id: 1,
      userId,
      items: [{ id: 1, productId: 101, quantity: 1 }],
      createdAt: new Date(),
      updatedAt: new Date(),
    };
    const updatedCart: Cart = {
      ...existingCart,
      items: [{ id: 1, productId: 101, quantity: 3 }],
    };
    MockedPostgresCartRepository.prototype.findByUserId.mockResolvedValue(
      existingCart,
    );
    MockedPostgresCartRepository.prototype.save.mockResolvedValue(updatedCart);

    // Act
    const res = await request(app)
      .put(`/api/v1/carts/${userId}/items/101`)
      .send(update);

    // Assert
    expect(res.statusCode).toEqual(200);
    const item = res.body.items.find((i: any) => i.productId === 101);
    expect(item.quantity).toEqual(3);
  });

  it("should delete an item from the cart", async () => {
    // Arrange
    const existingCart: Cart = {
      id: 1,
      userId,
      items: [{ id: 1, productId: 101, quantity: 1 }],
      createdAt: new Date(),
      updatedAt: new Date(),
    };
    const updatedCart: Cart = { ...existingCart, items: [] }; // Empty items array after delete
    MockedPostgresCartRepository.prototype.findByUserId.mockResolvedValue(
      existingCart,
    );
    MockedPostgresCartRepository.prototype.save.mockResolvedValue(updatedCart);

    // Act
    const res = await request(app).delete(`/api/v1/carts/${userId}/items/101`);

    // Assert
    expect(res.statusCode).toEqual(200);
    expect(res.body.items.length).toBe(0);
  });
});

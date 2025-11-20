const request = require("supertest");
const app = require("../../src/app");

describe("Cart API", () => {
  const userId = 123;

  it("should return a 404 for a non-existent cart", async () => {
    const res = await request(app).get(`/api/v1/carts/999`);
    expect(res.statusCode).toEqual(404);
  });

  it("should add an item to the cart", async () => {
    const newItem = { productId: 101, quantity: 1 };
    const res = await request(app)
      .post(`/api/v1/carts/${userId}/items`)
      .send(newItem);

    expect(res.statusCode).toEqual(200);
    expect(res.body.userId).toEqual(userId);
    expect(res.body.items.length).toBe(1);
  });

  it("should update an item quantity", async () => {
    const update = { quantity: 3 };
    const res = await request(app)
      .put(`/api/v1/carts/${userId}/items/101`)
      .send(update);

    expect(res.statusCode).toEqual(200);
    const item = res.body.items.find((i) => i.productId === 101);
    expect(item.quantity).toEqual(3);
  });

  it("should delete an item from the cart", async () => {
    const res = await request(app).delete(`/api/v1/carts/${userId}/items/101`);

    expect(res.statusCode).toEqual(200);
    expect(res.body.items.length).toBe(0);
  });
});

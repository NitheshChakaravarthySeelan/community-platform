import type { Cart } from "../models/cart";
import type { CartRepository } from "./CartRepository";
import { dbPool } from "../utils/db";

export class PostgresCartRepository implements CartRepository {
  async findByUserId(userId: number): Promise<Cart | null> {
    const client = await dbPool.connect();

    try {
      // SQL query to select the cart by userid
      const result = await client.query(
        "SELECT * FROM carts WHERE user_id = $1",
        [userId],
      );

      if (result.rows.length === 0) {
        return null;
      }

      const dbRow = result.rows[0];

      const cart: Cart = {
        id: dbRow.id,
        userId: dbRow.user_id,
        items: dbRow.items,
        createdAt: dbRow.created_at,
        updatedAt: dbRow.updated_at,
      };
      return cart;
    } catch (error) {
      console.error("Error finding cart by userId:", error);
      throw error;
    } finally {
      client.release();
    }
  }

  async save(cart: Omit<Cart, "id"> | Cart): Promise<Cart> {
    const client = await dbPool.connect();

    try {
      const itemsJson = JSON.stringify(cart.items);
      const query = `
				INSERT INTO carts (user_id, items, created_at, updated_at)
				VALUES ($1, $2, NOW(), NOW())
				ON CONFLICT (user_id) DO UPDATE SET
					items = EXCLUDED.items,
					updated_at = NOW()
				RETURNING *;
				`;

      const values = [cart.userId, itemsJson];
      const result = await client.query(query, values);

      if (result.rows.length === 0) {
        throw new Error(
          "Failed to save cart: Now row returned after insert/update.",
        );
      }

      const dbRow = result.rows[0];

      const savedCart: Cart = {
        id: dbRow.id,
        userId: dbRow.user_id,
        items: dbRow.items,
        createdAt: dbRow.created_at,
        updatedAt: dbRow.updated_at,
      };

      return savedCart;
    } catch (error) {
      console.error("Error saving cart:", error);
      throw error;
    } finally {
      client.release();
    }
  }
}

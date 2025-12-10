import axios from "axios";

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  quantity: number;
  sku: string;
  imageUrl: string;
  category: string;
  manufacturer: string;
  status: string;
  version: number;
  createdAt: Date;
  updatedAt: Date;
}

export class ProductServiceAdapter {
  private readonly baseUrl: string;

  constructor() {
    this.baseUrl = "http://product_read_dev:8081/api/v1/products";
  }

  /**
   * Fetch a single product by its ID from the product-read service.
   * @param productId The ID of the product to fetch.
   * @returns The product date or null if not found.
   */
  public async getProductById(productId: number): Promise<Product | null> {
    try {
      const response = await axios.get<Product>(`${this.baseUrl}/${productId}`);
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        return null;
      }
      console.error(`Error fetching product ${productId}:`, error);
      throw new Error("Failed to communicate with the product service.");
    }
  }
}

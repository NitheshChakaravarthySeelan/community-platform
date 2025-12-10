import axios from "axios";
export class ProductServiceAdapter {
  baseUrl;
  constructor() {
    this.baseUrl = "http://product_read_dev:8081/api/v1/products";
  }
  /**
   * Fetch a single product by its ID from the product-read service.
   * @param productId The ID of the product to fetch.
   * @returns The product date or null if not found.
   */
  async getProductById(productId) {
    try {
      const response = await axios.get(`${this.baseUrl}/${productId}`);
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
//# sourceMappingURL=ProductServiceAdapter.js.map

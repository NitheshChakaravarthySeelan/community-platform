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
export declare class ProductServiceAdapter {
    private readonly baseUrl;
    constructor();
    /**
     * Fetch a single product by its ID from the product-read service.
     * @param productId The ID of the product to fetch.
     * @returns The product date or null if not found.
     */
    getProductById(productId: number): Promise<Product | null>;
}
//# sourceMappingURL=ProductServiceAdapter.d.ts.map
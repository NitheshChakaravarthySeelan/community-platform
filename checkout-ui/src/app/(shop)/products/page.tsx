import { apiRequest } from "@/lib/api";
import { ProductCard } from "@/components/product/product-card";
import { Button } from "@/components/ui/button";
import Link from "next/link";
import { PlusIcon } from "lucide-react";

export const dynamic = "force-dynamic";

interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
}

export default async function ProductsPage() {
  let products: Product[] = [];
  let error: string | null = null;

  try {
    products = await apiRequest<Product[]>("/api/products");
  } catch (err: any) {
    console.error("Failed to fetch products:", err);
    error = err.message || "Could not load products.";
  }

  return (
    <div className="container py-8">
      <div className="flex flex-col md:flex-row justify-between items-center mb-8 gap-4">
        <h1 className="text-4xl font-extrabold tracking-tight">
          Our Collection
        </h1>
        <div className="flex gap-2">
          {/* Admin shortcut link - will be handled by middleware/component if needed, but for now just showing link */}
          <Link href="/admin/products/new">
            <Button size="sm" variant="outline" className="hidden md:flex">
              <PlusIcon className="mr-2 h-4 w-4" /> Add Product
            </Button>
          </Link>
        </div>
      </div>

      {error && (
        <div
          className="bg-destructive/10 border border-destructive text-destructive px-4 py-3 rounded relative mb-6"
          role="alert"
        >
          <strong className="font-bold">Error!</strong>
          <span className="block sm:inline ml-2">{error}</span>
        </div>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>

      {products.length === 0 && !error && (
        <div className="flex flex-col items-center justify-center py-20 text-center">
          <div className="rounded-full bg-muted p-6 mb-4">
            <ShoppingCartIcon className="h-12 w-12 text-muted-foreground" />
          </div>
          <h3 className="text-xl font-semibold">No products found</h3>
          <p className="text-muted-foreground mt-2 max-w-xs">
            We couldn't find any products in our catalog. Check back later or
            add some as an admin!
          </p>
        </div>
      )}
    </div>
  );
}

// Added ShoppingCartIcon for the empty state
function ShoppingCartIcon(props: any) {
  return (
    <svg
      {...props}
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="8" cy="21" r="1" />
      <circle cx="19" cy="21" r="1" />
      <path d="M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.1-5.38a1 1 0 0 0-1-1.21H5.14" />
    </svg>
  );
}

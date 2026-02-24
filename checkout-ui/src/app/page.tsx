import Link from "next/link";
import { Button } from "@/components/ui/button";
import { apiRequest } from "@/lib/api";
import { ProductCard } from "@/components/product/product-card";

interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
}

export default async function Home() {
  let products: Product[] = [];
  try {
    const data = await apiRequest<Product[]>("/api/products");
    products = data.slice(0, 3); // Just show top 3 on home page
  } catch (err) {
    console.error("Failed to fetch featured products", err);
  }

  return (
    <div className="flex flex-col items-center justify-center">
      {/* Hero Section */}
      <section className="w-full py-12 md:py-24 lg:py-32 xl:py-48 bg-gradient-to-br from-background to-muted border-b">
        <div className="container px-4 md:px-6 mx-auto">
          <div className="flex flex-col items-center space-y-6 text-center">
            <h1 className="text-4xl font-extrabold tracking-tight sm:text-5xl md:text-6xl lg:text-7xl/none text-foreground">
              Community Microservices Shop
            </h1>
            <p className="mx-auto max-w-[900px] text-muted-foreground md:text-xl leading-relaxed">
              Experience a modern, distributed e-commerce platform built with
              Spring Boot, Node.js, and Next.js.
            </p>
            <div className="space-x-4">
              <Link href="/products">
                <Button size="lg" className="shadow-lg">
                  Shop Now
                </Button>
              </Link>
              <Link href="/register">
                <Button variant="outline" size="lg" className="shadow-lg">
                  Join Community
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Featured Products Section */}
      <section className="w-full py-12 md:py-24 lg:py-32">
        <div className="container px-4 md:px-6 mx-auto">
          <div className="flex flex-col items-center justify-center space-y-4 text-center mb-10">
            <h2 className="text-3xl font-bold tracking-tighter sm:text-4xl uppercase">
              Featured Selection
            </h2>
            <div className="h-1 w-20 bg-primary"></div>
          </div>

          {products.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              {products.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          ) : (
            <div className="text-center py-10 bg-muted/50 rounded-xl border-2 border-dashed">
              <p className="text-muted-foreground">
                No products added yet. Head to Admin to create some!
              </p>
              <Link href="/admin/products" className="mt-4 inline-block">
                <Button variant="secondary">Go to Dashboard</Button>
              </Link>
            </div>
          )}

          <div className="flex justify-center mt-12">
            <Link href="/products">
              <Button variant="link" className="text-lg">
                View All Products &rarr;
              </Button>
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}

"use client";

import { useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";
import { ProductCard } from "@/components/product/product-card";
import { Loader2 } from "lucide-react";

export default function SearchPage() {
  const searchParams = useSearchParams();
  const query = searchParams.get("q");
  const [results, setResults] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    async function fetchResults() {
      if (!query) return;
      setLoading(true);
      try {
        const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || "";
        const res = await fetch(
          `${baseUrl}/api/search?q=${encodeURIComponent(query)}`,
        );
        const data = await res.json();
        setResults(data.products || []);
      } catch (err) {
        console.error("Failed to fetch search results:", err);
      } finally {
        setLoading(false);
      }
    }
    fetchResults();
  }, [query]);

  return (
    <div className="container py-10">
      <h1 className="text-3xl font-bold mb-6">
        {query ? `Search results for "${query}"` : "Search Products"}
      </h1>

      {loading ? (
        <div className="flex justify-center items-center h-64">
          <Loader2 className="h-10 w-10 animate-spin text-primary" />
        </div>
      ) : results.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {results.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      ) : (
        <div className="text-center py-20 bg-muted/30 rounded-lg border-2 border-dashed">
          <p className="text-xl text-muted-foreground">
            No products found matching your search.
          </p>
        </div>
      )}
    </div>
  );
}

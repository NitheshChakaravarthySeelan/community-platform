"use client";

import Image from "next/image";
import Link from "next/link";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { AddToCartButton } from "@/components/cart/add-to-cart-button";
import { useAuth } from "@/hooks/useAuth";
import { Settings2Icon } from "lucide-react";

interface ProductCardProps {
  product: {
    id: string;
    name: string;
    description: string;
    price: number;
    imageUrl: string;
  };
}

export function ProductCard({ product }: ProductCardProps) {
  const { isAdmin } = useAuth();

  return (
    <Card className="flex flex-col overflow-hidden transition-all duration-300 hover:shadow-lg relative group">
      {isAdmin && (
        <Link
          href={`/admin/products`}
          className="absolute top-2 right-2 z-10 bg-background/80 backdrop-blur-sm p-2 rounded-full border shadow-sm hover:bg-background transition-colors"
          title="Manage Product"
        >
          <Settings2Icon className="h-4 w-4 text-orange-600" />
        </Link>
      )}
      <Link
        href={`/products/${product.id}`}
        className="relative block aspect-video w-full overflow-hidden"
      >
        <Image
          src={product.imageUrl || "/placeholder.svg"}
          alt={product.name}
          fill
          className="object-cover transition-transform duration-500 group-hover:scale-110"
        />
      </Link>
      <CardHeader className="p-4 flex-grow">
        <CardTitle className="text-lg font-bold line-clamp-1">
          <Link
            href={`/products/${product.id}`}
            className="hover:text-primary transition-colors"
          >
            {product.name}
          </Link>
        </CardTitle>
        <CardDescription className="text-sm line-clamp-2 min-h-[40px]">
          {product.description}
        </CardDescription>
      </CardHeader>
      <CardContent className="p-4 pt-0">
        <div className="flex items-center justify-between">
          <p className="text-xl font-extrabold text-primary">
            ${(product.price / 100).toFixed(2)}
          </p>
        </div>
      </CardContent>
      <CardFooter className="p-4 pt-0 gap-2">
        <AddToCartButton productId={product.id} />
      </CardFooter>
    </Card>
  );
}

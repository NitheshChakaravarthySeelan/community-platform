"use client"; // This component must be a Client Component

import Link from "next/link";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { AuthProvider, useAuth } from "@/hooks/useAuth"; // Import useAuth
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { ShoppingCartIcon, SearchIcon } from "lucide-react";
import { useCart } from "@/hooks/useCart";
import { Input } from "@/components/ui/input";
import { useState, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useDebounce } from "@/hooks/useDebounce";
import { Suspense } from "react";

function HeaderInternal() {
  const { isAuthenticated, user, logout } = useAuth();
  const { cart } = useCart();
  const router = useRouter();
  const searchParams = useSearchParams();
  const [searchTerm, setSearchTerm] = useState(searchParams.get("q") || "");
  const debouncedSearchTerm = useDebounce(searchTerm, 300);

  const cartItemCount =
    cart?.items?.reduce((acc, item) => acc + item.quantity, 0) || 0;

  useEffect(() => {
    if (debouncedSearchTerm) {
      router.push(`/search?q=${encodeURIComponent(debouncedSearchTerm)}`);
    } else if (searchTerm === "" && searchParams.get("q")) {
      router.push("/");
    }
  }, [debouncedSearchTerm, router, searchTerm, searchParams]);

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-14 items-center justify-between">
        <Link href="/" className="mr-6 flex items-center space-x-2">
          <span className="font-bold inline-block text-xl tracking-tight">
            COMMUNITY SHOP
          </span>
        </Link>
        <div className="hidden md:flex flex-1 items-center justify-center px-4">
          <div className="relative w-full max-w-md">
            <SearchIcon className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              type="search"
              placeholder="Search products..."
              className="w-full bg-background pl-8"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </div>
        <nav className="flex items-center space-x-4 lg:space-x-6">
          <Link
            href="/products"
            className="text-sm font-medium transition-colors hover:text-primary"
          >
            Products
          </Link>
          <Link
            href="/orders"
            className="text-sm font-medium transition-colors hover:text-primary"
          >
            Orders
          </Link>
          <Link
            href="/payments"
            className="text-sm font-medium transition-colors hover:text-primary"
          >
            Payments
          </Link>
          {user?.roles?.includes("ADMIN") && (
            <Link
              href="/admin/products"
              className="text-sm font-medium text-orange-600 transition-colors hover:text-orange-500"
            >
              Admin Dashboard
            </Link>
          )}
        </nav>
        <div className="flex flex-1 items-center justify-end space-x-4">
          <Link href="/cart" className="relative group p-2">
            <ShoppingCartIcon className="h-6 w-6 transition-colors group-hover:text-primary" />
            {cartItemCount > 0 && (
              <span className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-primary text-[10px] font-bold text-primary-foreground">
                {cartItemCount}
              </span>
            )}
          </Link>

          {isAuthenticated ? (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button
                  variant="ghost"
                  className="relative h-8 w-8 rounded-full"
                >
                  <Avatar className="h-8 w-8">
                    <AvatarImage
                      src={`https://api.dicebear.com/7.x/initials/svg?seed=${user?.userName.username}`}
                      alt={user?.userName.username}
                    />
                    <AvatarFallback>
                      {user?.userName.username.charAt(0).toUpperCase()}
                    </AvatarFallback>
                  </Avatar>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-56" align="end" forceMount>
                <DropdownMenuLabel className="font-normal">
                  <div className="flex flex-col space-y-1">
                    <p className="text-sm font-medium leading-none">
                      {user?.userName.username}
                    </p>
                    <p className="text-xs leading-none text-muted-foreground">
                      {user?.email}
                    </p>
                  </div>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  onClick={async () => {
                    try {
                      const response = await fetch("/api/auth/promote", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ email: user?.email }),
                      });
                      const data = await response.json();
                      if (data.success) {
                        alert(
                          "Promoted to ADMIN! Please log out and log back in to see changes.",
                        );
                      } else {
                        alert("Failed: " + data.message);
                      }
                    } catch (err) {
                      alert("Error promoting user");
                    }
                  }}
                >
                  Promote to Admin (Dev)
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={logout}>Log out</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          ) : (
            <>
              <Link href="/login">
                <Button variant="ghost">Login</Button>
              </Link>
              <Link href="/register">
                <Button variant="default">Sign Up</Button>
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

// Header Component using shadcn/ui principles and better Tailwind
export function Header() {
  return (
    <Suspense fallback={<div className="h-14 w-full border-b bg-background" />}>
      <HeaderInternal />
    </Suspense>
  );
}

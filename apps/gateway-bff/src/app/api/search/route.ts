import { NextRequest, NextResponse } from "next/server";
import { searchClient } from "@/lib/grpc/search.client";
import { SearchRequest, SearchResponse } from "@/proto/search";
import * as grpc from "@grpc/grpc-js";

export async function GET(request: NextRequest) {
  const { searchParams } = new URL(request.url);
  const q = searchParams.get("q");

  if (!q) {
    return NextResponse.json({ products: [] });
  }

  const searchRequest: SearchRequest = {
    query: q,
    limit: 20,
    offset: 0,
  };

  try {
    const products = await new Promise((resolve, reject) => {
      searchClient.searchProducts(
        searchRequest,
        (error: grpc.ServiceError | null, response: SearchResponse) => {
          if (error) {
            reject(error);
          } else {
            resolve(response.products);
          }
        },
      );
    });

    return NextResponse.json({ products });
  } catch (error) {
    console.error("Search gRPC call failed:", error);
    return NextResponse.json({ error: "Search failed" }, { status: 500 });
  }
}

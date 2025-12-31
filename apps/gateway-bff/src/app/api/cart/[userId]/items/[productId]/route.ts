import { NextResponse, type NextRequest } from "next/server";
import { proxy } from "@/lib/httpResponse";

// PUT /api/cart/[userId]/items/[productId]
export async function PUT(
  request: NextRequest,
  { params }: { params: { userId: string; productId: string } },
) {
  try {
    const { userId, productId } = params;
    const body = await request.json();
    const option = {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    };
    const response = await proxy(
      `${process.env.CART_CRUD_SERVICE_URL}/${userId}/items/${productId}`,
      option,
    );
    return response;
  } catch (error: Error | unknown) {
    return NextResponse.json({
      error: error instanceof Error ? error.message : "Something went wrong",
    });
  }
}

// DELETE /api/cart/[userId]/items/[productId]
export async function DELETE(
  request: NextRequest,
  { params }: { params: { userId: string; productId: string } },
) {
  try {
    const { userId, productId } = params;
    const option = {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
      },
    };
    const response = await proxy(
      `${process.env.CART_CRUD_SERVICE_URL}/${userId}/items/${productId}`,
      option,
    );
    return response;
  } catch (error: Error | unknown) {
    return NextResponse.json({
      error: error instanceof Error ? error.message : "Something went wrong",
    });
  }
}

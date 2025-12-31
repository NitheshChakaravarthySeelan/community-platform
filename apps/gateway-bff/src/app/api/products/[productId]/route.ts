import { NextResponse, type NextRequest } from "next/server";
import { proxy } from "@/lib/httpResponse";

export async function GET(
  request: NextRequest,
  { params }: { params: { productId: string } },
) {
  try {
    const { productId } = params;
    const option = {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    };
    const response = await proxy(
      `${process.env.PRODUCT_READ_SERVICE_URL}/${productId}`,
      option,
    );
    return response;
  } catch (error: Error | unknown) {
    return NextResponse.json({
      error: error instanceof Error ? error.message : "Something went wrong",
    });
  }
}

export async function PUT(
  request: NextRequest,
  { params }: { params: { productId: string } },
) {
  try {
    const { productId } = params;
    const body = await request.json();
    const headers = new Headers(request.headers);
    const userId = headers.get("X-User-ID");
    const userRoles = headers.get("X-User-Roles");

    const option: RequestInit = {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        ...(userId && { "X-User-ID": userId }),
        ...(userRoles && { "X-User-Roles": userRoles }),
      },
      body: JSON.stringify(body),
    };
    const response = await proxy(
      `${process.env.PRODUCT_WRITE_SERVICE_URL}/${productId}`,
      option,
    );
    return response;
  } catch (error: Error | unknown) {
    return NextResponse.json({
      error: error instanceof Error ? error.message : "Something went wrong",
    });
  }
}

export async function DELETE(
  request: NextRequest,
  { params }: { params: { productId: string } },
) {
  try {
    const { productId } = params;
    const headers = new Headers(request.headers);
    const userId = headers.get("X-User-ID");
    const userRoles = headers.get("X-User-Roles");

    const option: RequestInit = {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        ...(userId && { "X-User-ID": userId }),
        ...(userRoles && { "X-User-Roles": userRoles }),
      },
    };
    const response = await proxy(
      `${process.env.PRODUCT_WRITE_SERVICE_URL}/${productId}`,
      option,
    );
    return response;
  } catch (error: Error | unknown) {
    return NextResponse.json({
      error: error instanceof Error ? error.message : "Something went wrong",
    });
  }
}

import { NextResponse, type NextRequest } from "next/server";
import { proxy } from "@/lib/httpResponse";

export async function GET(request: NextRequest) {
  try {
    const option = {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    };
    const response = await proxy(
      `${process.env.PRODUCT_READ_SERVICE_URL}`,
      option,
    );
    return response;
  } catch (error: Error | unknown) {
    return NextResponse.json({
      error: error instanceof Error ? error.message : "Something went wrong",
    });
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const headers = new Headers(request.headers);
    // Forward X-User-ID and X-User-Roles from incoming request to backend
    // These headers would typically be set by a middleware or authentication system upstream
    // For local testing, they might be manually added or handled by a development proxy
    const userId = headers.get("X-User-ID");
    const userRoles = headers.get("X-User-Roles");

    const option: RequestInit = {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(userId && { "X-User-ID": userId }),
        ...(userRoles && { "X-User-Roles": userRoles }),
      },
      body: JSON.stringify(body),
    };
    const response = await proxy(
      `${process.env.PRODUCT_WRITE_SERVICE_URL}`,
      option,
    );
    return response;
  } catch (error: Error | unknown) {
    return NextResponse.json({
      error: error instanceof Error ? error.message : "Something went wrong",
    });
  }
}

import { NextResponse, type NextRequest } from "next/server";
import { proxy } from "@/lib/httpResponse";
import { cookies } from "next/headers";

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const option = {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    };
    const response = await proxy(
      `${process.env.AUTH_SERVICE_URL}/login`,
      option,
    );
    const field = await response.json();

    if (field.success) {
      const jwtToken = field.data.jwtToken;
      const cookieStore = (await cookies()).set("jwt_token", jwtToken, {
        httpOnly: true,
        secure: process.env.NODE_ENV !== "production",
        sameSite: "strict",
        path: "/",
      });
    }
    return response;
  } catch (error: Error | unknown) {
    return NextResponse.json({
      error: error instanceof Error ? error.message : "Something went wrong",
    });
  }
}

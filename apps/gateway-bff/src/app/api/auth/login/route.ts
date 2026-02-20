import { NextResponse } from "next/server";
import { cookies } from "next/headers";

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const serviceUrl = `${process.env.AUTH_SERVICE_URL}/api/auth/login`;
    console.log(`Proxying login request to: ${serviceUrl}`);

    const response = await fetch(serviceUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });

    const data = await response.json();

    if (response.ok) {
      const jwtToken = data.jwtToken;
      const res = NextResponse.json(
        { success: true, data },
        { status: response.status },
      );
      res.cookies.set("jwt_token", jwtToken, {
        httpOnly: true,
        secure: process.env.NODE_ENV !== "production",
        sameSite: "strict",
        path: "/",
      });
      return res;
    } else {
      console.error(`Login failed with status ${response.status}:`, data);
      return NextResponse.json(
        { success: false, ...data },
        { status: response.status },
      );
    }
  } catch (error: Error | unknown) {
    const message =
      error instanceof Error ? error.message : "Something went wrong";
    console.error(`Login route exception: ${message}`);
    return NextResponse.json({ success: false, message }, { status: 500 });
  }
}

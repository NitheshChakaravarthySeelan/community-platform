import { NextResponse } from "next/server";

export async function POST(request: Request) {
  try {
    const body = await request.json();
    // Directly hit the auth-service on its host port (3002)
    const serviceUrl = "http://localhost:3002/api/auth/promote";
    console.log(`[Local Proxy] Promoting user via: ${serviceUrl}`);

    const response = await fetch(serviceUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });

    const data = await response.json();
    return NextResponse.json(data, { status: response.status });
  } catch (error: Error | unknown) {
    const message =
      error instanceof Error ? error.message : "Something went wrong";
    console.error(`Local Promote route exception: ${message}`);
    return NextResponse.json({ success: false, message }, { status: 500 });
  }
}

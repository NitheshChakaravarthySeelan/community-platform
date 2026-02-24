import { NextResponse } from "next/server";

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const serviceUrl = `${process.env.AUTH_SERVICE_URL}/api/auth/promote`;
    console.log(`Proxying promote request to: ${serviceUrl}`);

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
    console.error(`Promote route exception: ${message}`);
    return NextResponse.json({ success: false, message }, { status: 500 });
  }
}

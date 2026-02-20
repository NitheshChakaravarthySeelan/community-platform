import { NextResponse } from "next/server";

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const serviceUrl = `${process.env.AUTH_SERVICE_URL}/api/auth/register`;
    console.log(`Proxying register request to: ${serviceUrl}`);

    const response = await fetch(serviceUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });

    const contentType = response.headers.get("content-type");
    let data;
    if (contentType && contentType.includes("application/json")) {
      data = await response.json();
    } else {
      const text = await response.text();
      data = { message: text };
    }

    return NextResponse.json(data, { status: response.status });
  } catch (error: Error | unknown) {
    const message =
      error instanceof Error ? error.message : "Something went wrong";
    console.error(`Register route exception: ${message}`);
    return NextResponse.json({ success: false, message }, { status: 500 });
  }
}

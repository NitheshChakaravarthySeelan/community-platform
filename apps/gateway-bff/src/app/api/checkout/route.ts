import { NextResponse, type NextRequest } from "next/server";
import { proxy } from "@/lib/httpResponse";

export async function POST(request: NextRequest) {
  try {
    const authUserId = request.headers.get("X-User-ID");
    if (!authUserId) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const body = await request.json();

    // Security: Ensure the user is only checking out their own cart
    if (body.user_id !== authUserId) {
      return NextResponse.json(
        { error: "Forbidden: You can only checkout your own cart" },
        { status: 403 },
      );
    }

    const option = {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-User-ID": authUserId,
        "X-User-Roles": request.headers.get("X-User-Roles") || "",
      },
      body: JSON.stringify(body),
    };

    // Note: proxy function should handle the fetch call correctly
    const response = await proxy(
      `${process.env.CHECKOUT_ORCHESTRATOR_URL}/api/checkout`,
      option,
    );
    return response;
  } catch (error: Error | unknown) {
    console.error("Gateway Checkout Error:", error);
    return NextResponse.json(
      {
        error: error instanceof Error ? error.message : "Something went wrong",
      },
      { status: 500 },
    );
  }
}

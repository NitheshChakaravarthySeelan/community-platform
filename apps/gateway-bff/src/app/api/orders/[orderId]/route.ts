import { NextResponse, type NextRequest } from "next/server";
import { proxy } from "@/lib/httpResponse";

export async function GET(
  request: NextRequest,
  { params }: { params: { orderId: string } },
) {
  try {
    const { orderId } = params;
    const option = {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    };
    const response = await proxy(
      `${process.env.ORDER_READ_SERVICE_URL}/${orderId}`,
      option,
    );
    return response;
  } catch (error: Error | unknown) {
    return NextResponse.json({
      error: error instanceof Error ? error.message : "Something went wrong",
    });
  }
}

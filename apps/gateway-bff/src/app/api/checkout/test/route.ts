import { NextResponse } from "next/server";

export async function GET() {
  return NextResponse.json(
    { message: "Checkout test route working!" },
    { status: 200 },
  );
}

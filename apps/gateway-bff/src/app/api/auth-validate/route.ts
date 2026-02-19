import { NextResponse, type NextRequest } from "next/server";
import { AuthGrpcClient } from "../../../lib/grpc/auth.client";

// Instantiate AuthGrpcClient
const authGrpcClient = new AuthGrpcClient(
  process.env.AUTH_SERVICE_GRPC_URL || "auth-service:6565",
);

export async function GET(request: NextRequest) {
  const token = request.headers.get("Authorization")?.split(" ")[1];

  if (!token) {
    return NextResponse.json({ error: "No token provided" }, { status: 400 });
  }

  try {
    const validationResult = await authGrpcClient.validateToken(token);
    return NextResponse.json(validationResult);
  } catch (error) {
    console.error("Authentication gRPC error:", error);
    return NextResponse.json(
      { error: "Token validation failed" },
      { status: 500 },
    );
  }
}

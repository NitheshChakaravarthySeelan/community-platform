import { NextResponse, type NextRequest } from "next/server";

export const config = {
  matcher: ["/api/products/:path*", "/api/orders/:path*"],
};

export async function middleware(request: NextRequest) {
  const jwtToken = request.cookies.get("jwtToken")?.value;

  if (!jwtToken) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  try {
    // Make an internal fetch call to the new API route for token validation
    const apiResponse = await fetch("http://localhost:3000/api/auth-validate", {
      headers: {
        Authorization: `Bearer ${jwtToken}`,
      },
    });

    if (!apiResponse.ok) {
      return NextResponse.json(
        { error: "Unauthorized" },
        { status: apiResponse.status },
      );
    }

    const validationResult = await apiResponse.json();

    if (!validationResult.isValid) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const newHeader = new Headers(request.headers);
    newHeader.set("X-User-ID", validationResult.userId);
    newHeader.set("X-User-Name", validationResult.userName);
    newHeader.set("X-User-Roles", validationResult.roles.join(","));
    return NextResponse.next({
      request: {
        headers: newHeader,
      },
    });
  } catch (error) {
    console.error("Token validation API error:", error);
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }
}

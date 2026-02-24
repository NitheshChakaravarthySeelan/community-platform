import { NextResponse, type NextRequest } from "next/server";

export const config = {
  matcher: ["/api/products/:path*", "/api/orders/:path*"],
};

export async function middleware(request: NextRequest) {
  // CORS handling
  const origin = request.headers.get("origin") || "";
  const allowedOrigins = ["http://localhost:3005", "http://localhost:3000"];
  const isAllowedOrigin = allowedOrigins.includes(origin);

  if (request.method === "OPTIONS") {
    return new NextResponse(null, {
      status: 200,
      headers: {
        "Access-Control-Allow-Origin": isAllowedOrigin
          ? origin
          : allowedOrigins[0],
        "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
        "Access-Control-Allow-Headers":
          "Content-Type, Authorization, X-User-ID, X-User-Name, X-User-Roles",
        "Access-Control-Allow-Credentials": "true",
      },
    });
  }

  const response = await handleMiddleware(request);

  if (isAllowedOrigin) {
    response.headers.set("Access-Control-Allow-Origin", origin);
    response.headers.set("Access-Control-Allow-Credentials", "true");
    response.headers.set(
      "Access-Control-Allow-Methods",
      "GET, POST, PUT, DELETE, OPTIONS",
    );
    response.headers.set(
      "Access-Control-Allow-Headers",
      "Content-Type, Authorization, X-User-ID, X-User-Name, X-User-Roles",
    );
  }

  return response;
}

async function handleMiddleware(request: NextRequest) {
  // Allow public GET access to products
  if (
    request.nextUrl.pathname.startsWith("/api/products") &&
    request.method === "GET"
  ) {
    return NextResponse.next();
  }

  const jwtToken = request.cookies.get("jwtToken")?.value;

  if (!jwtToken) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  try {
    // Make an internal fetch call to the new API route for token validation
    // Note: Inside Docker, localhost:3000 refers to the container itself.
    // Use the actual service name or relative URL if possible.
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

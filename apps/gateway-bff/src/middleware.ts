import { NextResponse, type NextRequest } from "next/server";
import { cookies } from "next/headers";

export const config = {
  matcher: ["/api/products/:path*", "/api/orders/:path*"],
};

export async function middleware(request: NextRequest) {
  const jwtToken = request.cookies.get("jwtToken")?.value;

  if (!jwtToken) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const options = {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${jwtToken}`,
    },
  };

  try {
    const response = await fetch(
      `${process.env.AUTH_SERVICE_URL}/api/auth/validate`,
      options,
    );

    if (response.status != 200) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const user = await response.json();
    const newHeader = new Headers(request.headers);
    newHeader.set("X-User_ID", user.id);
    newHeader.set("X-User-Name", user.name);
    newHeader.set("X-User-Roles", user.roles.join(","));
    return NextResponse.next({
      request: {
        headers: newHeader,
      },
    });
  } catch (error) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }
}

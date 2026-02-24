// lib/api.ts
const IS_SERVER = typeof window === "undefined";

const API_BASE_URL = IS_SERVER
  ? process.env.INTERNAL_API_BASE_URL || "http://localhost:3004"
  : process.env.NEXT_PUBLIC_API_BASE_URL || "";

interface RequestOptions extends RequestInit {
  token?: string;
}

export async function apiRequest<T>(
  endpoint: string,
  options?: RequestOptions,
): Promise<T> {
  const { token, headers, ...restOptions } = options || {};

  const url = `${API_BASE_URL}${endpoint}`;
  console.log(`Making API request: ${restOptions.method || "GET"} ${url}`);

  const config: RequestInit = {
    ...restOptions,
    headers: {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
      ...headers,
    },
  };

  const response = await fetch(url, config);

  if (!response.ok) {
    const contentType = response.headers.get("content-type");
    const errorData =
      contentType && contentType.includes("application/json")
        ? await response.json()
        : { message: await response.text() };

    console.error(`API request failed:`, errorData);
    throw new Error(
      errorData.message || errorData.error || "Something went wrong",
    );
  }

  return response.json() as Promise<T>;
}

// You might want to define specific API functions here later
// export const authApi = {
//   login: (credentials: any) => apiRequest("/api/auth/login", { method: "POST", body: JSON.stringify(credentials) }),
//   register: (data: any) => apiRequest("/api/auth/register", { method: "POST", body: JSON.stringify(data) }),
// };

// export const productApi = {
//   getAll: () => apiRequest("/api/products"),
//   getById: (id: string) => apiRequest(`/api/products/${id}`),
//   create: (productData: any, token: string) => apiRequest("/api/products", {
//     method: "POST",
//     headers: {
//       "Authorization": `Bearer ${token}`,
//       "X-User-ID": "1", // Placeholder, will get from auth context
//       "X-User-Roles": "ADMIN", // Placeholder, will get from auth context
//     },
//     body: JSON.stringify(productData),
//   }),
// };

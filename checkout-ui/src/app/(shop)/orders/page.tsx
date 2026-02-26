import React from "react";
import { apiRequest } from "@/lib/api";
import { format } from "date-fns";

interface OrderItem {
  productId: string;
  productName: string;
  quantity: number;
  price: number;
}

interface Order {
  orderId: string;
  userId: string;
  items: OrderItem[];
  totalAmount: number;
  status: string;
  orderDate: string;
}

export default async function OrdersPage() {
  let orders: Order[] = [];
  let error: string | null = null;

  try {
    // For demo purposes, we fetch all orders.
    // In a real app, we'd fetch for the logged-in user.
    orders = await apiRequest<Order[]>("/api/orders");
  } catch (err: any) {
    console.error("Failed to fetch orders:", err);
    error = err.message;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">My Orders</h1>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
          <p>Error: {error}</p>
        </div>
      )}

      {orders.length === 0 && !error ? (
        <div className="text-center py-12">
          <p className="text-gray-500 text-xl">
            You haven't placed any orders yet.
          </p>
        </div>
      ) : (
        <div className="space-y-6">
          {orders.map((order) => (
            <div
              key={order.orderId}
              className="border rounded-lg shadow-sm overflow-hidden bg-white"
            >
              <div className="bg-gray-50 px-6 py-4 border-b flex justify-between items-center">
                <div>
                  <p className="text-sm text-gray-500 uppercase font-semibold">
                    Order Placed
                  </p>
                  <p className="font-medium">
                    {format(new Date(order.orderDate), "PPP")}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-gray-500 uppercase font-semibold text-right">
                    Total
                  </p>
                  <p className="font-medium text-right">
                    ${order.totalAmount.toFixed(2)}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-gray-500 uppercase font-semibold text-right">
                    Status
                  </p>
                  <span
                    className={`inline-block px-3 py-1 rounded-full text-xs font-bold ${
                      order.status === "COMPLETED"
                        ? "bg-green-100 text-green-800"
                        : order.status === "PENDING"
                          ? "bg-yellow-100 text-yellow-800"
                          : "bg-gray-100 text-gray-800"
                    }`}
                  >
                    {order.status}
                  </span>
                </div>
                <div>
                  <p className="text-sm text-gray-500 uppercase font-semibold text-right">
                    Order ID
                  </p>
                  <p className="text-sm font-mono">
                    #{order.orderId.slice(0, 8)}
                  </p>
                </div>
              </div>
              <div className="px-6 py-4">
                <ul className="divide-y">
                  {order.items.map((item, idx) => (
                    <li
                      key={idx}
                      className="py-4 flex justify-between items-center"
                    >
                      <div className="flex items-center">
                        <div className="ml-4">
                          <p className="font-semibold text-gray-900">
                            {item.productName}
                          </p>
                          <p className="text-sm text-gray-500">
                            Qty: {item.quantity}
                          </p>
                        </div>
                      </div>
                      <p className="font-medium">${item.price.toFixed(2)}</p>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

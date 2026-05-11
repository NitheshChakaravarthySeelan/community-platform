"use client";

import React, { useState, useEffect, Suspense } from "react";
import { apiRequest } from "@/lib/api";
import { useRouter, useSearchParams } from "next/navigation";

function PaymentsPageInternal() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const [orderId, setOrderId] = useState("");
  const [amount, setAmount] = useState("");
  const [currency, setCurrency] = useState("USD");
  const [paymentMethod, setPaymentMethod] = useState("CREDIT_CARD");
  const [cardNumber, setCardNumber] = useState("");
  const [isProcessing, setIsProcessing] = useState(false);
  const [status, setStatus] = useState<null | "SUCCESS" | "FAILED">(null);
  const [message, setMessage] = useState("");

  useEffect(() => {
    const qOrderId = searchParams.get("orderId");
    const qAmount = searchParams.get("amount");
    if (qOrderId) setOrderId(qOrderId);
    if (qAmount) setAmount(qAmount);
  }, [searchParams]);

  const handlePayment = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsProcessing(true);
    setStatus(null);
    setMessage("");

    try {
      const result = await apiRequest<{ status: string; message: string }>(
        "/api/payments/process-payment",
        {
          method: "POST",
          body: JSON.stringify({
            orderId,
            amount: parseFloat(amount),
            currency,
            paymentMethod,
            paymentMethodDetails: {
              cardNumber,
              expiry: "12/26",
              cvv: "123",
            },
          }),
        },
      );

      if (result.status === "SUCCESS") {
        setStatus("SUCCESS");
        setMessage("Your payment has been processed successfully.");
        setTimeout(() => router.push("/orders"), 2000);
      } else {
        setStatus("FAILED");
        setMessage("Payment failed: " + (result.message || "Unknown error"));
      }
    } catch (err: any) {
      console.error("Payment error:", err);
      setStatus("FAILED");
      setMessage(
        err.message ||
          "An unexpected error occurred during payment processing.",
      );
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="max-w-md mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8 text-center">Checkout Payment</h1>

      {status === "SUCCESS" && (
        <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-6 text-center">
          <p className="font-bold text-lg">Payment Successful!</p>
          <p>{message}</p>
          <p className="text-sm mt-2 text-green-600 font-semibold italic">
            Redirecting to orders...
          </p>
        </div>
      )}

      {status === "FAILED" && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6 text-center">
          <p className="font-bold">Payment Failed</p>
          <p>{message}</p>
        </div>
      )}

      <form
        onSubmit={handlePayment}
        className="bg-white border rounded-lg shadow-sm p-8 space-y-6"
      >
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            Order ID
          </label>
          <input
            type="text"
            className="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 py-3 px-4 border"
            placeholder="e.g. 550e8400-e29b-41d4-a716-446655440000"
            value={orderId}
            onChange={(e) => setOrderId(e.target.value)}
            required
          />
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            Amount
          </label>
          <div className="relative">
            <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500">
              $
            </span>
            <input
              type="number"
              step="0.01"
              className="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 py-3 pl-8 pr-4 border"
              placeholder="0.00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              required
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            Payment Method
          </label>
          <select
            className="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 py-3 px-4 border"
            value={paymentMethod}
            onChange={(e) => setPaymentMethod(e.target.value)}
          >
            <option value="CREDIT_CARD">Credit Card</option>
            <option value="PAYPAL">PayPal</option>
            <option value="WALLET">Wallet</option>
          </select>
        </div>

        {paymentMethod === "CREDIT_CARD" && (
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Card Number
            </label>
            <input
              type="text"
              className="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 py-3 px-4 border font-mono"
              placeholder="**** **** **** ****"
              value={cardNumber}
              onChange={(e) => setCardNumber(e.target.value)}
              required
            />
          </div>
        )}

        <button
          type="submit"
          disabled={isProcessing || status === "SUCCESS"}
          className={`w-full py-4 px-4 rounded-md shadow font-bold text-lg text-white transition-colors duration-200 ${
            isProcessing || status === "SUCCESS"
              ? "bg-gray-400 cursor-not-allowed"
              : "bg-blue-600 hover:bg-blue-700 active:bg-blue-800"
          }`}
        >
          {isProcessing ? "Processing..." : "Pay Now"}
        </button>
      </form>
    </div>
  );
}

export default function PaymentsPage() {
  return (
    <Suspense
      fallback={
        <div className="container py-8 text-center">Loading payment...</div>
      }
    >
      <PaymentsPageInternal />
    </Suspense>
  );
}

import { NextRequest, NextResponse } from "next/server";
import { Kafka, Producer } from "kafkajs";
import { v4 as uuidv4 } from "uuid";

// --- Event Interface (Matching Java DTO) ---
interface CheckoutInitiatedEvent {
  orderId: string; // UUID from Java maps to string in TS
  userId: string; // UUID from Java maps to string in TS
  items: Array<{ productId: string; quantity: number }>; // Matching InventoryItem in Java DTO
  totalAmount: number; // BigDecimal from Java maps to number in TS
  timestamp: string; // Instant from Java maps to string in TS
  type: string; // Event type string
}
// --- End Event Interface ---

class ProducerManager {
  private producer: Producer;
  private isConnected: boolean = false;

  constructor() {
    const kafka = new Kafka({
      clientId: "gateway-bff-initiator",
      brokers: [process.env.KAFKA_BOOTSTRAP_SERVERS || "localhost:29092"],
    });
    this.producer = kafka.producer();
  }

  async connect() {
    if (!this.isConnected) {
      console.log("Connecting Kafka producer...");
      await this.producer.connect();
      this.isConnected = true;
      console.log("Kafka producer connected.");
    }
  }

  async disconnect() {
    if (this.isConnected) {
      console.log("Disconnecting Kafka producer...");
      await this.producer.disconnect();
      this.isConnected = false;
      console.log("Kafka producer disconnected.");
    }
  }

  async send(topic: string, messages: Array<{ key: string; value: string }>) {
    await this.connect(); // Ensure connected before sending
    await this.producer.send({ topic, messages });
  }
}

const producerManager = new ProducerManager();

export async function POST(req: NextRequest) {
  try {
    const { userId, items, totalAmount } = await req.json();

    if (
      !userId ||
      !items ||
      !Array.isArray(items) ||
      items.length === 0 ||
      !totalAmount
    ) {
      return NextResponse.json(
        { error: "Missing required fields: userId, items, totalAmount" },
        { status: 400 },
      );
    }

    const orderId = uuidv4(); // Generate a unique ID for this saga
    const timestamp = new Date().toISOString(); // Current timestamp

    const checkoutInitiatedEvent: CheckoutInitiatedEvent = {
      orderId: orderId,
      userId: userId,
      items: items.map((item: { productId: string; quantity: number }) => ({
        productId: item.productId,
        quantity: item.quantity,
      })),
      totalAmount: totalAmount,
      timestamp: timestamp,
      type: "CheckoutInitiatedEvent", // Explicitly set type
    };

    await producerManager.send("checkout.checkout-events", [
      {
        key: orderId, // Use orderId as the message key for partitioning
        value: JSON.stringify(checkoutInitiatedEvent),
      },
    ]);

    console.log(`CheckoutInitiatedEvent sent for Order ID: ${orderId}`);
    return NextResponse.json(
      {
        message: "Checkout initiated successfully",
        orderId: orderId,
        status: "PROCESSING",
      },
      { status: 202 },
    );
  } catch (error) {
    console.error("Failed to initiate checkout:", error);
    return NextResponse.json(
      { error: "Failed to initiate checkout" },
      { status: 500 },
    );
  }
}

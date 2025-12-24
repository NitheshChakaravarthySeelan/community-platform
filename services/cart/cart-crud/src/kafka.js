import { Kafka } from "kafkajs";
import { CartService } from "./services/cart.service";
// --- End Event Interfaces ---
const kafka = new Kafka({
    clientId: "cart-crud-service",
    brokers: [process.env.KAFKA_BOOTSTRAP_SERVERS || "localhost:29092"],
});
const consumer = kafka.consumer({ groupId: "cart-crud-group" });
const producer = kafka.producer();
export const initKafka = async (cartService) => {
    await producer.connect();
    await consumer.connect();
    await consumer.subscribe({
        topic: "checkout.checkout-events", // Changed to central event topic
        fromBeginning: false, // Start consuming from new messages by default
    });
    await consumer.run({
        eachMessage: async ({ topic, partition, message }) => {
            if (!message.value) {
                console.warn("Received null message from Kafka");
                return;
            }
            let payload;
            try {
                payload = JSON.parse(message.value.toString());
            }
            catch (error) {
                console.error(`Failed to parse message value as JSON: ${error}`);
                return;
            }
            console.log(`Received event of type: ${payload.type} for order: ${payload.orderId || "N/A"}`);
            switch (payload.type) {
                case "OrderCreatedEvent": // Handle OrderCreatedEvent
                    const orderCreatedEvent = payload; // Type assertion
                    try {
                        // Validate UUIDs
                        if (!orderCreatedEvent.orderId ||
                            typeof orderCreatedEvent.orderId !== "string") {
                            throw new Error("Invalid or missing orderId in OrderCreatedEvent.");
                        }
                        if (!orderCreatedEvent.userId ||
                            typeof orderCreatedEvent.userId !== "string") {
                            throw new Error("Invalid or missing userId in OrderCreatedEvent.");
                        }
                        await cartService.clearCartByUserId(orderCreatedEvent.userId);
                        const cartClearedEvent = {
                            orderId: orderCreatedEvent.orderId,
                            userId: orderCreatedEvent.userId,
                            timestamp: new Date().toISOString(), // Use ISO string for consistency with Java Instant
                            type: "CartClearedEvent",
                        };
                        await producer.send({
                            topic: "checkout.checkout-events",
                            messages: [{ value: JSON.stringify(cartClearedEvent) }],
                        });
                        console.log(`CartClearedEvent sent for order: ${orderCreatedEvent.orderId} and user: ${orderCreatedEvent.userId}`);
                    }
                    catch (error) {
                        console.error(`Failed to process OrderCreatedEvent for order ${orderCreatedEvent.orderId}, user ${orderCreatedEvent.userId}: ${error}`);
                        const cartClearanceFailedEvent = {
                            orderId: orderCreatedEvent.orderId,
                            userId: orderCreatedEvent.userId,
                            reason: error instanceof Error ? error.message : "Unknown error",
                            timestamp: new Date().toISOString(),
                            type: "CartClearanceFailedEvent",
                        };
                        await producer.send({
                            topic: "checkout.checkout-events",
                            messages: [
                                { value: JSON.stringify(cartClearanceFailedEvent) },
                            ],
                        });
                    }
                    break;
                case "OrderCreationFailedEvent": // Optional: handle if an order fails to be created
                    // This service might need to react if the order creation failed, e.g., to revert some cart state
                    console.warn(`OrderCreationFailedEvent received for order: ${payload.orderId || "N/A"}. No action taken by cart-crud.`);
                    break;
                // Add other event handlers here if needed
                default:
                    console.warn(`Unknown event type received: ${payload.type}`);
            }
        },
    });
};
export const disconnectKafka = async () => {
    await consumer.disconnect();
    await producer.disconnect();
};
//# sourceMappingURL=kafka.js.map
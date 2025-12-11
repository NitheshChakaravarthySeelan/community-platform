import { Kafka, Consumer, Producer } from "kafkajs";
import { CartService } from "./services/cart.service";
import { logger } from "./utils";

const kafka = new Kafka({
  clientId: "cart-crud-service",
  brokers: [process.env.KAFKA_BOOTSTRAP_SERVERS || "localhost:29092"],
});

const consumer: Consumer = kafka.consumer({ groupId: "cart-crud-group" });
const producer: Producer = kafka.producer();

export const initKafka = async (cartService: CartService) => {
  await producer.connect();
  await consumer.connect();

  await consumer.subscribe({
    topic: "checkout.cart-command",
    fromBeginning: true,
  });

  await consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      if (!message.value) {
        logger.warn("Received null message from Kafka");
        return;
      }

      const payload = JSON.parse(message.value.toString());
      const { type, saga_id, cart_id, user_id, event_id, reply_to_topic } =
        payload;

      logger.info(`Received command: ${type} for saga: ${saga_id}`);

      switch (type) {
        case "ClearCart":
          try {
            await cartService.clearCart(cart_id); // Assuming cartService has a clearCart method
            await producer.send({
              topic: reply_to_topic || "checkout.checkout-events",
              messages: [
                {
                  value: JSON.stringify({
                    type: "CartCleared",
                    saga_id,
                    cart_id,
                    user_id,
                    event_id: event_id || Date.now().toString(),
                  }),
                },
              ],
            });
            logger.info(`CartCleared event sent for saga: ${saga_id}`);
          } catch (error) {
            logger.error(`Failed to clear cart for saga ${saga_id}: ${error}`);
            await producer.send({
              topic: reply_to_topic || "checkout.checkout-events",
              messages: [
                {
                  value: JSON.stringify({
                    type: "CartClearanceFailed",
                    saga_id,
                    cart_id,
                    user_id,
                    reason:
                      error instanceof Error ? error.message : "Unknown error",
                    event_id: event_id || Date.now().toString(),
                  }),
                },
              ],
            });
          }
          break;
        // Add other command handlers here if needed
        default:
          logger.warn(`Unknown command type received: ${type}`);
      }
    },
  });
};

export const disconnectKafka = async () => {
  await consumer.disconnect();
  await producer.disconnect();
};

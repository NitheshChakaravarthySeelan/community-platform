package com.community.orders.paymentgateway.kafka;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @Autowired private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "checkout.payment-command", groupId = "payment-gateway-group")
    public void consume(String message) {
        System.out.println("Consumed message: " + message);
        JSONObject command = new JSONObject(message);
        String sagaId = command.getString("saga_id");

        // Simulate payment processing
        boolean paymentSuccess = Math.random() > 0.1; // 90% success rate

        JSONObject event = new JSONObject();
        event.put("saga_id", sagaId);
        if (paymentSuccess) {
            event.put("type", "PaymentProcessed");
            kafkaTemplate.send("checkout.checkout-events", event.toString());
        } else {
            event.put("type", "PaymentFailed");
            event.put("reason", "Payment provider declined the transaction.");
            kafkaTemplate.send("checkout.checkout-events", event.toString());
        }
    }
}

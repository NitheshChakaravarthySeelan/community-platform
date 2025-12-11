package com.community.orders.ordercreate.kafka;

import com.community.orders.ordercreate.domain.model.Order;
import com.community.orders.ordercreate.domain.model.Status;
import com.community.orders.ordercreate.domain.repository.OrderRepository;
import java.util.UUID;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @Autowired private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired private OrderRepository orderRepository;

    @KafkaListener(topics = "checkout.order-command", groupId = "order-create-group")
    public void consume(String message) {
        System.out.println("Consumed message: " + message);
        JSONObject command = new JSONObject(message);
        String sagaId = command.getString("saga_id");
        String userId = command.getString("user_id");
        JSONObject cartDetails = command.getJSONObject("cart_details");
        int totalCents = cartDetails.getInt("total_price"); // Assuming total_price is in cents

        try {
            Order order = new Order();
            order.setUserId(UUID.fromString(userId));
            order.setTotalCents(totalCents);
            order.setStatus(Status.PENDING_PAYMENT); // Set initial status
            Order savedOrder = orderRepository.save(order);

            JSONObject event = new JSONObject();
            event.put("saga_id", sagaId);
            event.put("order_id", savedOrder.getId().toString());
            event.put("type", "OrderCreated");
            kafkaTemplate.send("checkout.checkout-events", event.toString());

        } catch (Exception e) {
            JSONObject event = new JSONObject();
            event.put("saga_id", sagaId);
            event.put("type", "OrderCreationFailed");
            event.put("reason", e.getMessage());
            kafkaTemplate.send("checkout.checkout-events", event.toString());
        }
    }
}

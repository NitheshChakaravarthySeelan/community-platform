package com.community.orders.paymentgateway.kafka;

import java.time.Instant;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.community.orders.paymentgateway.application.dto.ProcessPaymentCommand;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import com.community.orders.paymentgateway.application.dto.PaymentProcessedEvent;
import com.community.orders.paymentgateway.application.dto.PaymentFailedEvent;

@Component
public class KafkaConsumer {

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @KafkaListener(topics = "checkout.payment-command", groupId = "payment-gateway-group")
  public void consume(ProcessPaymentCommand message) {
    System.out.println("Consumed message: " + message);

    // Simulate payment processing
    boolean paymentSuccess = Math.random() > 0.1; // 90% success rate

    if (paymentSuccess) {
      PaymentProcessedEvent successEvent = new PaymentProcessedEvent();
      successEvent.setOrderId(message.getOrderId);
      successEvent.setPaymentId(message.getPaymentId);
      successEvent.setTimestamp(Instant.now());
      kafkaTemplate.send("checkout.checkout-events", successEvent);
    } else {
      PaymentFailedEvent failedEvent = new PaymentFailedEvent();
      failedEvent.setOrderId(message.getOrderId);
      failedEvent.setReason(message.getReason);
      failedEvent.setTimestamp(Instant.now());
      kafkaTemplate.send("checkout.checkout-events", failedEvent);
    }
  }
}

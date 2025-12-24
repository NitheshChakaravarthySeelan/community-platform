package com.community.orders.paymentgateway.kafka;

import com.community.orders.paymentgateway.application.service.PaymentService;
import com.community.orders.paymentgateway.interfaces.dto.PaymentRequest;
import com.community.orders.paymentgateway.interfaces.dto.PaymentResponse;
import com.community.platform.shared.kafka.dto.CheckoutInitiatedEvent; // New DTO to consume
import com.community.platform.shared.kafka.dto.PaymentFailedEvent;
import com.community.platform.shared.kafka.dto.PaymentProcessedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentService paymentService;

    @Value("${topic.checkout.checkout-events}")
    private String checkoutEventsTopic;

    public KafkaConsumer(
            KafkaTemplate<String, Object> kafkaTemplate, PaymentService paymentService) {
        this.kafkaTemplate = kafkaTemplate;
        this.paymentService = paymentService;
    }

    // Listen to the central checkout events topic
    @KafkaListener(topics = "${topic.checkout.checkout-events}", groupId = "payment-gateway-group")
    public void consume(CheckoutInitiatedEvent event) { // Consume CheckoutInitiatedEvent
        System.out.println("Consumed CheckoutInitiatedEvent for order: " + event.getOrderId());

        // In a multi-event listener, you'd check event.getType() here, but for now,
        // this listener's primary task is to process the initiated event.
        // We're simplifying to assume CheckoutInitiatedEvent is the only thing this listener
        // processes on this topic that triggers payment.

        PaymentRequest paymentRequest = mapToRequest(event);
        PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);

        if ("SUCCESS".equals(paymentResponse.getStatus())) {
            PaymentProcessedEvent successEvent = new PaymentProcessedEvent();
            successEvent.setOrderId(paymentResponse.getOrderId());
            successEvent.setPaymentId(paymentResponse.getTransactionId());
            // userId and amount must be propagated from the CheckoutInitiatedEvent
            successEvent.setUserId(event.getUserId());
            successEvent.setAmount(event.getTotalAmount());
            successEvent.setTimestamp(paymentResponse.getTimestamp());
            kafkaTemplate.send(checkoutEventsTopic, successEvent);
        } else {
            PaymentFailedEvent failedEvent = new PaymentFailedEvent();
            failedEvent.setOrderId(paymentResponse.getOrderId());
            failedEvent.setReason(paymentResponse.getMessage());
            failedEvent.setTimestamp(paymentResponse.getTimestamp());
            kafkaTemplate.send(checkoutEventsTopic, failedEvent);
        }
    }

    // Helper to map CheckoutInitiatedEvent to PaymentRequest
    private PaymentRequest mapToRequest(CheckoutInitiatedEvent event) {
        // Assumptions:
        // - PaymentRequest doesn't need all details from CheckoutInitiatedEvent's items
        // - userId from event will be used
        // - totalAmount from event will be used
        return new PaymentRequest(
                event.getOrderId(), // Use orderId from the initiated event
                event.getTotalAmount(), // Use totalAmount from the initiated event
                "USD", // Default currency
                "CREDIT_CARD", // Default payment method
                null); // No specific method details for now
    }
}

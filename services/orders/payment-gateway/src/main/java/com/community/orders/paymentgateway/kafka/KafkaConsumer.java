package com.community.orders.paymentgateway.kafka;

import com.community.orders.paymentgateway.application.service.PaymentService;
import com.community.orders.paymentgateway.interfaces.dto.PaymentResponse;
import com.community.platform.shared.proto.common.SagaMetadata;
import com.community.platform.shared.proto.payment.PaymentFailedEvent;
import com.community.platform.shared.proto.payment.PaymentProcessedEvent;
import com.community.platform.shared.proto.payment.ProcessPaymentCommand;
import com.community.platform.shared.proto.payment.RefundPaymentCommand;
import com.google.protobuf.Timestamp;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentService paymentService;

    @Value("${topic.checkout.checkout-events}")
    private String checkoutEventsTopic;

    @KafkaListener(topics = "checkout.payment-command", groupId = "payment-gateway-group")
    public void consumeProcessPayment(ProcessPaymentCommand command) {
        log.info("Consumed ProcessPaymentCommand for saga: {}", command.getMetadata().getSagaId());

        PaymentResponse response = paymentService.processPayment(command);

        if ("SUCCESS".equals(response.getStatus())) {
            PaymentProcessedEvent successEvent =
                    PaymentProcessedEvent.newBuilder()
                            .setMetadata(
                                    SagaMetadata.newBuilder()
                                            .setSagaId(command.getMetadata().getSagaId())
                                            .setEventId(UUID.randomUUID().toString())
                                            .setTimestamp(
                                                    Timestamp.newBuilder()
                                                            .setSeconds(
                                                                    Instant.now().getEpochSecond())
                                                            .build())
                                            .build())
                            .setTransactionId(response.getTransactionId().toString())
                            .setOrderId(response.getOrderId().toString())
                            .setUserId(command.getUserId())
                            .setAmountCents(command.getAmountCents())
                            .setStatus("SUCCESS")
                            .build();
            kafkaTemplate.send(checkoutEventsTopic, successEvent);
        } else {
            PaymentFailedEvent failedEvent =
                    PaymentFailedEvent.newBuilder()
                            .setMetadata(
                                    SagaMetadata.newBuilder()
                                            .setSagaId(command.getMetadata().getSagaId())
                                            .setEventId(UUID.randomUUID().toString())
                                            .setTimestamp(
                                                    Timestamp.newBuilder()
                                                            .setSeconds(
                                                                    Instant.now().getEpochSecond())
                                                            .build())
                                            .build())
                            .setOrderId(command.getOrderId())
                            .setUserId(command.getUserId())
                            .setReason(response.getMessage())
                            .build();
            kafkaTemplate.send(checkoutEventsTopic, failedEvent);
        }
    }

    @KafkaListener(topics = "checkout.payment-command", groupId = "payment-gateway-group")
    public void consumeRefundPayment(RefundPaymentCommand command) {
        log.info("Consumed RefundPaymentCommand for saga: {}", command.getMetadata().getSagaId());
        paymentService.refundPayment(command);
        // Produce PaymentRefundedEvent if needed
    }
}

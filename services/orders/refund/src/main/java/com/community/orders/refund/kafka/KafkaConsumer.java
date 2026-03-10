package com.community.orders.refund.kafka;

import com.community.orders.refund.application.service.RefundService;
import com.community.orders.refund.interfaces.dto.RefundResponse;
import com.community.platform.shared.proto.common.SagaMetadata;
import com.community.platform.shared.proto.payment.PaymentRefundedEvent;
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
    private final RefundService refundService;

    @Value("${topic.checkout.checkout-events:checkout.checkout-events}")
    private String checkoutEventsTopic;

    @KafkaListener(topics = "checkout.payment-command", groupId = "refund-service-group")
    public void consumeRefundPayment(RefundPaymentCommand command) {
        log.info("Consumed RefundPaymentCommand for saga: {}", command.getMetadata().getSagaId());
        try {
            RefundResponse response = refundService.processRefund(command);
            PaymentRefundedEvent event =
                    PaymentRefundedEvent.newBuilder()
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
                            .setRefundTransactionId(response.getRefundId().toString())
                            .setOriginalTransactionId(command.getTransactionId())
                            .setAmountCents(command.getAmountCents())
                            .build();
            kafkaTemplate.send(checkoutEventsTopic, event);
        } catch (Exception e) {
            log.error(
                    "Failed to process refund for saga {}: {}",
                    command.getMetadata().getSagaId(),
                    e.getMessage());
        }
    }
}

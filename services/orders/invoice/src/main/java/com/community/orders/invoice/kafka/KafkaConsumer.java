package com.community.orders.invoice.kafka;

import com.community.orders.invoice.application.service.InvoiceService;
import com.community.orders.invoice.interfaces.dto.InvoiceResponse;
import com.community.platform.shared.proto.common.SagaMetadata;
import com.community.platform.shared.proto.invoice.*;
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
    private final InvoiceService invoiceService;

    @Value("${topic.checkout.checkout-events:checkout.checkout-events}")
    private String checkoutEventsTopic;

    @KafkaListener(topics = "checkout.invoice-command", groupId = "invoice-service-group")
    public void consumeGenerateInvoice(GenerateInvoiceCommand command) {
        log.info("Consumed GenerateInvoiceCommand for saga: {}", command.getMetadata().getSagaId());
        try {
            InvoiceResponse response = invoiceService.generateInvoice(command);
            InvoiceGeneratedEvent event =
                    InvoiceGeneratedEvent.newBuilder()
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
                            .setInvoiceId(response.getInvoiceId().toString())
                            .setOrderId(response.getOrderId().toString())
                            .setInvoicePdfUrl(
                                    "http://cdn.community.com/invoices/"
                                            + response.getInvoiceId()
                                            + ".pdf")
                            .build();
            kafkaTemplate.send(checkoutEventsTopic, event);
        } catch (Exception e) {
            log.error(
                    "Failed to generate invoice for saga {}: {}",
                    command.getMetadata().getSagaId(),
                    e.getMessage());
            InvoiceGenerationFailedEvent failedEvent =
                    InvoiceGenerationFailedEvent.newBuilder()
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
                            .setReason(e.getMessage())
                            .build();
            kafkaTemplate.send(checkoutEventsTopic, failedEvent);
        }
    }
}

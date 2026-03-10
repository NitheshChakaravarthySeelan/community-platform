package com.community.orders.wallet.kafka;

import com.community.orders.wallet.application.service.WalletService;
import com.community.orders.wallet.interfaces.dto.WalletResponse;
import com.community.platform.shared.proto.common.SagaMetadata;
import com.community.platform.shared.proto.wallet.*;
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
    private final WalletService walletService;

    @Value("${topic.checkout.checkout-events:checkout.checkout-events}")
    private String checkoutEventsTopic;

    @KafkaListener(topics = "checkout.wallet-command", groupId = "wallet-service-group")
    public void consumeDebitWallet(DebitWalletCommand command) {
        log.info("Consumed DebitWalletCommand for saga: {}", command.getMetadata().getSagaId());
        try {
            WalletResponse response = walletService.debitWallet(command);
            WalletDebitedEvent event =
                    WalletDebitedEvent.newBuilder()
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
                            .setUserId(command.getUserId())
                            .setDebitedAmountCents(command.getAmountCents())
                            .setCurrentBalanceCents(
                                    response.getNewBalance()
                                            .multiply(new java.math.BigDecimal(100))
                                            .longValue())
                            .build();
            kafkaTemplate.send(checkoutEventsTopic, event);
        } catch (Exception e) {
            log.error(
                    "Failed to debit wallet for saga {}: {}",
                    command.getMetadata().getSagaId(),
                    e.getMessage());
            WalletDebitFailedEvent failedEvent =
                    WalletDebitFailedEvent.newBuilder()
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
                            .setUserId(command.getUserId())
                            .setReason(e.getMessage())
                            .build();
            kafkaTemplate.send(checkoutEventsTopic, failedEvent);
        }
    }

    @KafkaListener(topics = "checkout.wallet-command", groupId = "wallet-service-group")
    public void consumeCreditWallet(CreditWalletCommand command) {
        log.info("Consumed CreditWalletCommand for saga: {}", command.getMetadata().getSagaId());
        try {
            WalletResponse response = walletService.creditWallet(command);
            WalletCreditedEvent event =
                    WalletCreditedEvent.newBuilder()
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
                            .setUserId(command.getUserId())
                            .setCreditedAmountCents(command.getAmountCents())
                            .setCurrentBalanceCents(
                                    response.getNewBalance()
                                            .multiply(new java.math.BigDecimal(100))
                                            .longValue())
                            .build();
            kafkaTemplate.send(checkoutEventsTopic, event);
        } catch (Exception e) {
            log.error(
                    "Failed to credit wallet for saga {}: {}",
                    command.getMetadata().getSagaId(),
                    e.getMessage());
        }
    }
}

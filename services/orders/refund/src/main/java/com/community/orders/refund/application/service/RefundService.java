package com.community.orders.refund.application.service;

import com.community.orders.refund.domain.model.Refund;
import com.community.orders.refund.domain.repository.RefundRepository;
import com.community.orders.refund.interfaces.dto.RefundResponse;
import com.community.platform.shared.proto.payment.RefundPaymentCommand;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;

    @Transactional
    public RefundResponse processRefund(RefundPaymentCommand command) {
        String sagaId = command.getMetadata().getSagaId();

        // Idempotency check
        Optional<Refund> existing = refundRepository.findBySagaId(sagaId);
        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        Refund refund =
                Refund.builder()
                        .sagaId(sagaId)
                        .transactionId(UUID.fromString(command.getTransactionId()))
                        .amount(
                                BigDecimal.valueOf(command.getAmountCents())
                                        .divide(BigDecimal.valueOf(100)))
                        .reason(command.getReason())
                        .status("COMPLETED")
                        .refundDate(Instant.now())
                        // In a real scenario, we might need orderId and userId in the command too
                        .build();

        Refund savedRefund = refundRepository.save(refund);
        return mapToResponse(savedRefund);
    }

    @Transactional(readOnly = true)
    public RefundResponse getRefundByOrderId(UUID orderId) {
        Refund refund =
                refundRepository
                        .findByOrderId(orderId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Refund not found for order ID: " + orderId));
        return mapToResponse(refund);
    }

    private RefundResponse mapToResponse(Refund refund) {
        return new RefundResponse(
                refund.getRefundId(),
                refund.getOrderId(),
                refund.getUserId(),
                refund.getAmount(),
                refund.getStatus(),
                refund.getReason(),
                refund.getRefundDate(),
                refund.getTransactionId());
    }
}

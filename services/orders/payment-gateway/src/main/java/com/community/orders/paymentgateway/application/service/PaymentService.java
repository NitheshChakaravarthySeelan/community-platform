package com.community.orders.paymentgateway.application.service;

import com.community.orders.paymentgateway.domain.model.PaymentTransaction;
import com.community.orders.paymentgateway.domain.repository.PaymentTransactionRepository;
import com.community.orders.paymentgateway.interfaces.dto.PaymentResponse;
import com.community.platform.shared.proto.payment.ProcessPaymentCommand;
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
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;

    @Transactional
    public PaymentResponse processPayment(ProcessPaymentCommand command) {
        String sagaId = command.getMetadata().getSagaId();

        // Idempotency check
        Optional<PaymentTransaction> existing = paymentTransactionRepository.findBySagaId(sagaId);
        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        BigDecimal amount =
                BigDecimal.valueOf(command.getAmountCents()).divide(BigDecimal.valueOf(100));

        // Simulate payment processing logic
        boolean paymentSuccessful = amount.compareTo(new BigDecimal("13.00")) != 0;
        String status = paymentSuccessful ? "SUCCESS" : "FAILED";
        String message =
                paymentSuccessful ? "Payment processed successfully." : "Insufficient funds.";

        PaymentTransaction transaction =
                PaymentTransaction.builder()
                        .sagaId(sagaId)
                        .orderId(UUID.fromString(command.getOrderId()))
                        .amount(amount)
                        .currency(command.getCurrency())
                        .paymentMethod(command.getPaymentMethod())
                        .status(status)
                        .message(message)
                        .timestamp(Instant.now())
                        .build();

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);
        return mapToResponse(savedTransaction);
    }

    @Transactional
    public void refundPayment(RefundPaymentCommand command) {
        // Implementation for refund logic
        String sagaId = command.getMetadata().getSagaId();
        // Check if already refunded (idempotency)
        // ...
    }

    private PaymentResponse mapToResponse(PaymentTransaction transaction) {
        return new PaymentResponse(
                transaction.getTransactionId(),
                transaction.getOrderId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getMessage(),
                transaction.getTimestamp());
    }
}

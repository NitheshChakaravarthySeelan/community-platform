package com.community.orders.paymentgateway.application.service;

import com.community.orders.paymentgateway.domain.model.PaymentTransaction;
import com.community.orders.paymentgateway.domain.repository.PaymentTransactionRepository;
import com.community.orders.paymentgateway.interfaces.dto.PaymentRequest;
import com.community.orders.paymentgateway.interfaces.dto.PaymentResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final Random random = new Random();

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // Simulate payment processing with deterministic logic
        // For testing purposes: an amount of 13.00 will simulate a failure.
        boolean paymentSuccessful = !request.getAmount().equals(new BigDecimal("13.00"));

        String status = paymentSuccessful ? "SUCCESS" : "FAILED";
        String message =
                paymentSuccessful
                        ? "Payment processed successfully."
                        : "Payment failed due to simulated error: insufficient funds.";

        PaymentTransaction transaction =
                PaymentTransaction.builder()
                        .orderId(request.getOrderId())
                        .amount(request.getAmount())
                        .currency(request.getCurrency())
                        .paymentMethod(request.getPaymentMethod())
                        .status(status)
                        .message(message)
                        .timestamp(Instant.now())
                        .build();

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);

        return mapToResponse(savedTransaction);
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

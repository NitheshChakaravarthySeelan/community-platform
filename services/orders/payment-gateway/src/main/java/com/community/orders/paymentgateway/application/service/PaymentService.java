package com.community.orders.paymentgateway.application.service;

import com.community.orders.paymentgateway.domain.model.PaymentTransaction;
import com.community.orders.paymentgateway.domain.repository.PaymentTransactionRepository;
import com.community.orders.paymentgateway.interfaces.dto.PaymentRequest;
import com.community.orders.paymentgateway.interfaces.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final Random random = new Random();

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // Simulate payment processing
        boolean paymentSuccessful = random.nextBoolean(); // Simulate success/failure randomly

        String status = paymentSuccessful ? "SUCCESS" : "FAILED";
        String message = paymentSuccessful ? "Payment processed successfully." : "Payment failed due to an unknown error.";

        PaymentTransaction transaction = PaymentTransaction.builder()
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
                transaction.getTimestamp()
        );
    }
}

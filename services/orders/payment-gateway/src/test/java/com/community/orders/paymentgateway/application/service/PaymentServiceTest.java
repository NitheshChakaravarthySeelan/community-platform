package com.community.orders.paymentgateway.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.community.orders.paymentgateway.domain.model.PaymentTransaction;
import com.community.orders.paymentgateway.domain.repository.PaymentTransactionRepository;
import com.community.orders.paymentgateway.interfaces.dto.PaymentRequest;
import com.community.orders.paymentgateway.interfaces.dto.PaymentResponse;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock private PaymentTransactionRepository paymentTransactionRepository;

    @InjectMocks private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        // Mock the repository's save method to return the transaction it was given
        when(paymentTransactionRepository.save(any(PaymentTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void whenProcessPayment_withNormalAmount_thenReturnsSuccess() {
        // Arrange
        PaymentRequest request =
                new PaymentRequest(
                        UUID.randomUUID(), new BigDecimal("100.00"), "USD", "CREDIT_CARD", null);

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(request.getOrderId(), response.getOrderId());
        assertEquals(request.getAmount(), response.getAmount());
    }

    @Test
    void whenProcessPayment_withFailureAmount_thenReturnsFailed() {
        // Arrange
        PaymentRequest request =
                new PaymentRequest(
                        UUID.randomUUID(), new BigDecimal("13.00"), "USD", "CREDIT_CARD", null);

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertEquals("FAILED", response.getStatus());
        assertEquals(
                "Payment failed due to simulated error: insufficient funds.",
                response.getMessage());
        assertEquals(request.getOrderId(), response.getOrderId());
    }
}

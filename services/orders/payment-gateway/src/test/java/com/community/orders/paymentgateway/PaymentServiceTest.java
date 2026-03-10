package com.community.orders.paymentgateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.community.orders.paymentgateway.application.service.PaymentService;
import com.community.orders.paymentgateway.domain.model.PaymentTransaction;
import com.community.orders.paymentgateway.domain.repository.PaymentTransactionRepository;
import com.community.orders.paymentgateway.interfaces.dto.PaymentResponse;
import com.community.platform.shared.proto.common.SagaMetadata;
import com.community.platform.shared.proto.payment.ProcessPaymentCommand;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PaymentServiceTest {

    private PaymentService paymentService;

    @Mock private PaymentTransactionRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = new PaymentService(repository);
    }

    @Test
    void testProcessPayment_Success() {
        String sagaId = "test-saga-123";
        ProcessPaymentCommand command =
                ProcessPaymentCommand.newBuilder()
                        .setMetadata(SagaMetadata.newBuilder().setSagaId(sagaId).build())
                        .setOrderId(UUID.randomUUID().toString())
                        .setUserId(UUID.randomUUID().toString())
                        .setAmountCents(1000) // 10.00
                        .setCurrency("USD")
                        .setPaymentMethod("CREDIT_CARD")
                        .build();

        when(repository.findBySagaId(sagaId)).thenReturn(Optional.empty());
        when(repository.save(any(PaymentTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processPayment(command);

        assertEquals("SUCCESS", response.getStatus());
        verify(repository, times(1)).save(any());
    }

    @Test
    void testProcessPayment_Idempotency() {
        String sagaId = "duplicate-saga";
        ProcessPaymentCommand command =
                ProcessPaymentCommand.newBuilder()
                        .setMetadata(SagaMetadata.newBuilder().setSagaId(sagaId).build())
                        .setOrderId(UUID.randomUUID().toString())
                        .setUserId(UUID.randomUUID().toString())
                        .setAmountCents(1000)
                        .build();

        PaymentTransaction existing =
                PaymentTransaction.builder()
                        .transactionId(UUID.randomUUID())
                        .sagaId(sagaId)
                        .status("SUCCESS")
                        .amount(new BigDecimal("10.00"))
                        .build();

        when(repository.findBySagaId(sagaId)).thenReturn(Optional.of(existing));

        PaymentResponse response = paymentService.processPayment(command);

        assertEquals("SUCCESS", response.getStatus());
        verify(repository, never()).save(any()); // Should not save again
    }

    @Test
    void testProcessPayment_SimulatedFailure() {
        String sagaId = "fail-saga";
        ProcessPaymentCommand command =
                ProcessPaymentCommand.newBuilder()
                        .setMetadata(SagaMetadata.newBuilder().setSagaId(sagaId).build())
                        .setOrderId(UUID.randomUUID().toString())
                        .setUserId(UUID.randomUUID().toString())
                        .setAmountCents(1300) // 13.00 is a simulated failure amount
                        .setCurrency("USD")
                        .build();

        when(repository.findBySagaId(sagaId)).thenReturn(Optional.empty());
        when(repository.save(any(PaymentTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processPayment(command);

        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Insufficient funds"));
    }
}

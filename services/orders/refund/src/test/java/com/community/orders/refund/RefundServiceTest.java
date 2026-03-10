package com.community.orders.refund;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.community.orders.refund.application.service.RefundService;
import com.community.orders.refund.domain.model.Refund;
import com.community.orders.refund.domain.repository.RefundRepository;
import com.community.orders.refund.interfaces.dto.RefundResponse;
import com.community.platform.shared.proto.common.SagaMetadata;
import com.community.platform.shared.proto.payment.RefundPaymentCommand;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RefundServiceTest {

    private RefundService refundService;

    @Mock private RefundRepository refundRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        refundService = new RefundService(refundRepository);
    }

    @Test
    void testProcessRefund_Success() {
        String sagaId = "refund-saga-1";
        RefundPaymentCommand command =
                RefundPaymentCommand.newBuilder()
                        .setMetadata(SagaMetadata.newBuilder().setSagaId(sagaId).build())
                        .setTransactionId(UUID.randomUUID().toString())
                        .setAmountCents(2000)
                        .build();

        when(refundRepository.findBySagaId(sagaId)).thenReturn(Optional.empty());
        when(refundRepository.save(any()))
                .thenAnswer(
                        i -> {
                            Refund r = i.getArgument(0);
                            r.setRefundId(UUID.randomUUID());
                            return r;
                        });

        RefundResponse response = refundService.processRefund(command);

        assertEquals("COMPLETED", response.getStatus());
        verify(refundRepository).save(any());
    }
}

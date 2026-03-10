package com.community.orders.invoice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.community.orders.invoice.application.service.InvoiceService;
import com.community.orders.invoice.domain.model.Invoice;
import com.community.orders.invoice.domain.repository.InvoiceRepository;
import com.community.orders.invoice.interfaces.dto.InvoiceResponse;
import com.community.platform.shared.proto.common.SagaMetadata;
import com.community.platform.shared.proto.invoice.GenerateInvoiceCommand;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InvoiceServiceTest {

    private InvoiceService invoiceService;

    @Mock private InvoiceRepository invoiceRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        invoiceService = new InvoiceService(invoiceRepository);
    }

    @Test
    void testGenerateInvoice_Success() {
        String sagaId = "invoice-saga-1";
        GenerateInvoiceCommand command =
                GenerateInvoiceCommand.newBuilder()
                        .setMetadata(SagaMetadata.newBuilder().setSagaId(sagaId).build())
                        .setOrderId(UUID.randomUUID().toString())
                        .setUserId(UUID.randomUUID().toString())
                        .setTotalCents(5000)
                        .build();

        when(invoiceRepository.findBySagaId(sagaId)).thenReturn(Optional.empty());
        when(invoiceRepository.save(any()))
                .thenAnswer(
                        i -> {
                            Invoice inv = i.getArgument(0);
                            inv.setInvoiceId(UUID.randomUUID());
                            return inv;
                        });

        InvoiceResponse response = invoiceService.generateInvoice(command);

        assertNotNull(response.getInvoiceId());
        assertEquals("PAID", response.getPaymentStatus());
        verify(invoiceRepository).save(any());
    }
}

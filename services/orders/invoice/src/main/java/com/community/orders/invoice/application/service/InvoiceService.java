package com.community.orders.invoice.application.service;

import com.community.orders.invoice.domain.model.Invoice;
import com.community.orders.invoice.domain.model.InvoiceItem;
import com.community.orders.invoice.domain.repository.InvoiceRepository;
import com.community.orders.invoice.interfaces.dto.InvoiceItemDTO;
import com.community.orders.invoice.interfaces.dto.InvoiceResponse;
import com.community.platform.shared.proto.invoice.GenerateInvoiceCommand;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public InvoiceResponse generateInvoice(GenerateInvoiceCommand command) {
        String sagaId = command.getMetadata().getSagaId();

        // Idempotency check
        Optional<Invoice> existing = invoiceRepository.findBySagaId(sagaId);
        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        List<InvoiceItem> invoiceItems =
                command.getItemsList().stream()
                        .map(
                                item ->
                                        InvoiceItem.builder()
                                                .productId(UUID.fromString(item.getProductId()))
                                                .productName(item.getName())
                                                .quantity(item.getQuantity())
                                                .unitPrice(
                                                        BigDecimal.valueOf(item.getUnitPriceCents())
                                                                .divide(BigDecimal.valueOf(100)))
                                                .totalPrice(
                                                        BigDecimal.valueOf(
                                                                        item.getTotalPriceCents())
                                                                .divide(BigDecimal.valueOf(100)))
                                                .build())
                        .collect(Collectors.toList());

        Invoice invoice =
                Invoice.builder()
                        .sagaId(sagaId)
                        .orderId(UUID.fromString(command.getOrderId()))
                        .userId(UUID.fromString(command.getUserId()))
                        .items(invoiceItems)
                        .totalAmount(
                                BigDecimal.valueOf(command.getTotalCents())
                                        .divide(BigDecimal.valueOf(100)))
                        .currency("USD")
                        .invoiceDate(Instant.now())
                        .paymentStatus("PAID") // In our saga, it's paid before invoice generation
                        .build();

        invoiceItems.forEach(item -> item.setInvoice(invoice));
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return mapToResponse(savedInvoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByOrderId(UUID orderId) {
        Invoice invoice =
                invoiceRepository
                        .findByOrderId(orderId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Invoice not found for order ID: " + orderId));
        return mapToResponse(invoice);
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        List<InvoiceItemDTO> itemDTOs =
                invoice.getItems().stream()
                        .map(
                                item ->
                                        new InvoiceItemDTO(
                                                item.getProductId(),
                                                item.getProductName(),
                                                item.getQuantity(),
                                                item.getUnitPrice(),
                                                item.getTotalPrice()))
                        .collect(Collectors.toList());

        return new InvoiceResponse(
                invoice.getInvoiceId(),
                invoice.getOrderId(),
                invoice.getUserId(),
                itemDTOs,
                invoice.getTotalAmount(),
                invoice.getCurrency(),
                invoice.getInvoiceDate(),
                invoice.getPaymentStatus());
    }
}

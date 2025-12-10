package com.community.orders.invoice.application.service;

import com.community.orders.invoice.domain.model.Invoice;
import com.community.orders.invoice.domain.model.InvoiceItem;
import com.community.orders.invoice.domain.repository.InvoiceRepository;
import com.community.orders.invoice.interfaces.dto.InvoiceItemDTO;
import com.community.orders.invoice.interfaces.dto.InvoiceRequest;
import com.community.orders.invoice.interfaces.dto.InvoiceResponse;
import java.time.Instant;
import java.util.List;
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
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        List<InvoiceItem> invoiceItems =
                request.getItems().stream()
                        .map(
                                itemDTO ->
                                        InvoiceItem.builder()
                                                .productId(itemDTO.getProductId())
                                                .productName(itemDTO.getProductName())
                                                .quantity(itemDTO.getQuantity())
                                                .unitPrice(itemDTO.getUnitPrice())
                                                .totalPrice(itemDTO.getTotalPrice())
                                                .build())
                        .collect(Collectors.toList());

        Invoice invoice =
                Invoice.builder()
                        .orderId(request.getOrderId())
                        .userId(request.getUserId())
                        .items(invoiceItems)
                        .totalAmount(request.getTotalAmount())
                        .currency(request.getCurrency())
                        .invoiceDate(Instant.now())
                        .paymentStatus("PENDING") // Default status
                        .build();

        // Establish bi-directional relationship
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
                        .map(this::mapInvoiceItemToDTO)
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

    private InvoiceItemDTO mapInvoiceItemToDTO(InvoiceItem item) {
        return new InvoiceItemDTO(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice());
    }
}

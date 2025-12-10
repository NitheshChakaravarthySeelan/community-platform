package com.community.orders.invoice.interfaces.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private UUID invoiceId;
    private UUID orderId;
    private UUID userId;
    private List<InvoiceItemDTO> items;
    private BigDecimal totalAmount;
    private String currency;
    private Instant invoiceDate;
    private String paymentStatus; // e.g., "PAID", "PENDING", "OVERDUE"
}

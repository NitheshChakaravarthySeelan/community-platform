package com.community.orders.invoice.interfaces.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {
    private UUID orderId;
    private UUID userId;
    private List<InvoiceItemDTO> items; // Assuming InvoiceItemDTO will be defined
    private BigDecimal totalAmount;
    private String currency; // e.g., "USD", "EUR"
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class InvoiceItemDTO {
    private UUID productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}

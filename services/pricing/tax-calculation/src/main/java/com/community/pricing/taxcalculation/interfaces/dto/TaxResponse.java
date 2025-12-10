package com.community.pricing.taxcalculation.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxResponse {
    private List<TaxItemResponseDTO> items;
    private BigDecimal totalNetPrice;
    private BigDecimal totalTaxAmount;
    private BigDecimal totalGrossPrice;
    private String currency; // Assuming a default currency or determined by context
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class TaxItemResponseDTO {
    private UUID productId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal netPrice; // unitPrice * quantity
    private BigDecimal taxRateApplied;
    private BigDecimal taxAmount;
    private BigDecimal grossPrice; // netPrice + taxAmount
}

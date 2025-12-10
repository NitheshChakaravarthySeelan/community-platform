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
public class TaxRequest {
    private List<TaxItemDTO> items;
    private TaxAddressDTO destinationAddress;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class TaxItemDTO {
    private UUID productId;
    private int quantity;
    private BigDecimal unitPrice;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class TaxAddressDTO {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}

package com.community.pricing.taxcalculation.interfaces.dto;

import java.util.List;
import lombok.Data;

// A simple representation of cart and address details for the request
@Data
public class TaxRequest {
    private String cartId;
    private AddressDTO shippingAddress;
    private List<CartItemDTO> items;

    @Data
    public static class AddressDTO {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }

    @Data
    public static class CartItemDTO {
        private String productId;
        private int quantity;
        private long priceCents;
    }
}

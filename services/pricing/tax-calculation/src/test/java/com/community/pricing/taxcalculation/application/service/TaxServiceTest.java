package com.community.pricing.taxcalculation.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.community.pricing.taxcalculation.domain.model.TaxRule;
import com.community.pricing.taxcalculation.domain.repository.TaxRuleRepository;
import com.community.pricing.taxcalculation.interfaces.dto.TaxRequest;
import com.community.pricing.taxcalculation.interfaces.dto.TaxResponse;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaxServiceTest {

    @Mock private TaxRuleRepository taxRuleRepository;

    @InjectMocks private TaxService taxService;

    private TaxRequest request;
    private TaxRule stateRule;

    @BeforeEach
    void setUp() {
        TaxRequest.AddressDTO address = new TaxRequest.AddressDTO();
        address.setCountry("US");
        address.setState("CA");

        TaxRequest.CartItemDTO item = new TaxRequest.CartItemDTO();
        item.setProductId("prod123");
        item.setQuantity(2);
        item.setPriceCents(10000); // $100.00

        request = new TaxRequest();
        request.setShippingAddress(address);
        request.setItems(Collections.singletonList(item));

        stateRule = new TaxRule();
        stateRule.setCountry("US");
        stateRule.setState("CA");
        stateRule.setTaxRatePercentage(new BigDecimal("7.25")); // 7.25%
    }

    @Test
    void whenStateRuleExists_thenCalculatesTax() {
        // Arrange
        when(taxRuleRepository.findByCountryAndState(anyString(), anyString()))
                .thenReturn(Optional.of(stateRule));

        // Act
        TaxResponse response = taxService.calculateTax(request);

        // Assert
        // Total cart value = 2 * 10000 = 20000 cents
        // Tax = 20000 * 0.0725 = 1450 cents
        assertEquals(1450, response.getTaxCents());
    }

    @Test
    void whenNoRuleExists_thenTaxIsZero() {
        // Arrange
        when(taxRuleRepository.findByCountryAndState(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(taxRuleRepository.findByCountryAndStateIsNull(anyString()))
                .thenReturn(Optional.empty());

        // Act
        TaxResponse response = taxService.calculateTax(request);

        // Assert
        assertEquals(0, response.getTaxCents());
    }
}

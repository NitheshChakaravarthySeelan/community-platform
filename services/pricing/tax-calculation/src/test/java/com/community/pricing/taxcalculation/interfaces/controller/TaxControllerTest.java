package com.community.pricing.taxcalculation.interfaces.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.community.pricing.taxcalculation.application.service.TaxService;
import com.community.pricing.taxcalculation.interfaces.dto.TaxResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaxController.class)
class TaxControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private TaxService taxService;

    @Test
    void whenCalculateTax_thenReturnsServiceResponse() throws Exception {
        // Arrange
        when(taxService.calculateTax(any())).thenReturn(new TaxResponse(1234)); // Example tax

        String requestBody =
                "{\"shippingAddress\": {\"country\": \"US\", \"state\": \"CA\"}, \"items\": []}";

        // Act & Assert
        mockMvc.perform(
                        post("/api/tax/calculate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taxCents").value(1234));
    }
}

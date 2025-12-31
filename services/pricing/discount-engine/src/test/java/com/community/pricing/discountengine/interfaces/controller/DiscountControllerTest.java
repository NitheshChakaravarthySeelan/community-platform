package com.community.pricing.discountengine.interfaces.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.community.pricing.discountengine.application.service.DiscountService;
import com.community.pricing.discountengine.interfaces.dto.DiscountResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DiscountController.class)
class DiscountControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private DiscountService discountService;

    @Test
    void whenCalculateDiscounts_thenReturnsServiceResponse() throws Exception {
        // Arrange: Mock the service to return a specific response
        when(discountService.calculateDiscounts(any()))
                .thenReturn(new DiscountResponse(150)); // Example discount of 150 cents

        String requestBody = "{\"cartId\": \"c123\", \"userId\": \"u456\", \"items\": []}";

        // Act & Assert
        mockMvc.perform(
                        post("/api/discounts/calculate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDiscountCents").value(150));
    }
}

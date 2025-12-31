package com.community.pricing.discountengine.interfaces.controller;

import com.community.pricing.discountengine.application.service.DiscountService;
import com.community.pricing.discountengine.interfaces.dto.DiscountRequest;
import com.community.pricing.discountengine.interfaces.dto.DiscountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping("/calculate")
    public DiscountResponse calculateDiscounts(@RequestBody DiscountRequest request) {
        return discountService.calculateDiscounts(request);
    }
}

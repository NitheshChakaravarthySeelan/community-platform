package com.community.pricing.taxcalculation.interfaces.controller;

import com.community.pricing.taxcalculation.application.service.TaxService;
import com.community.pricing.taxcalculation.interfaces.dto.TaxRequest;
import com.community.pricing.taxcalculation.interfaces.dto.TaxResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tax")
@RequiredArgsConstructor
public class TaxController {

    private final TaxService taxService;

    @PostMapping("/calculate")
    public TaxResponse calculateTax(@RequestBody TaxRequest request) {
        return taxService.calculateTax(request);
    }
}

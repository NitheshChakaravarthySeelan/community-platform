package com.community.pricing.taxcalculation.interfaces.controller;

import com.community.pricing.taxcalculation.application.service.TaxService;
import com.community.pricing.taxcalculation.interfaces.dto.TaxRequest;
import com.community.pricing.taxcalculation.interfaces.dto.TaxResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tax")
@RequiredArgsConstructor
public class TaxController {

    private final TaxService taxService;

    @PostMapping("/calculate")
    public ResponseEntity<TaxResponse> calculateTax(@RequestBody TaxRequest request) {
        TaxResponse response = taxService.calculateTax(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

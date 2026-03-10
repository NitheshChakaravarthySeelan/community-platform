package com.community.orders.paymentgateway.interfaces.controller;

import com.community.orders.paymentgateway.application.service.PaymentService;
import com.community.orders.paymentgateway.interfaces.dto.PaymentResponse;
import com.community.platform.shared.proto.payment.ProcessPaymentCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process-payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestBody ProcessPaymentCommand command) {
        PaymentResponse response = paymentService.processPayment(command);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

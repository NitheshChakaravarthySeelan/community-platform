package com.community.orders.refund.interfaces.controller;

import com.community.orders.refund.application.service.RefundService;
import com.community.orders.refund.interfaces.dto.RefundResponse;
import com.community.platform.shared.proto.payment.RefundPaymentCommand;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    public ResponseEntity<RefundResponse> processRefund(@RequestBody RefundPaymentCommand command) {
        RefundResponse response = refundService.processRefund(command);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<RefundResponse> getRefundByOrderId(@PathVariable UUID orderId) {
        RefundResponse response = refundService.getRefundByOrderId(orderId);
        return ResponseEntity.ok(response);
    }
}

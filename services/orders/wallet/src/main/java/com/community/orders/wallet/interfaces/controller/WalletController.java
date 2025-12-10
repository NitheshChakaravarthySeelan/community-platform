package com.community.orders.wallet.interfaces.controller;

import com.community.orders.wallet.application.service.WalletService;
import com.community.orders.wallet.interfaces.dto.WalletRequest;
import com.community.orders.wallet.interfaces.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/{userId}/credit")
    public ResponseEntity<WalletResponse> creditWallet(
            @PathVariable UUID userId,
            @RequestBody WalletRequest request) {
        WalletResponse response = walletService.creditWallet(userId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{userId}/debit")
    public ResponseEntity<WalletResponse> debitWallet(
            @PathVariable UUID userId,
            @RequestBody WalletRequest request) {
        WalletResponse response = walletService.debitWallet(userId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> getWalletBalance(@PathVariable UUID userId) {
        WalletResponse response = walletService.getWalletBalance(userId);
        return ResponseEntity.ok(response);
    }
}

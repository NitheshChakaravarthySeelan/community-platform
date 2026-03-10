package com.community.orders.wallet.interfaces.controller;

import com.community.orders.wallet.application.service.WalletService;
import com.community.orders.wallet.interfaces.dto.WalletResponse;
import com.community.platform.shared.proto.wallet.CreditWalletCommand;
import com.community.platform.shared.proto.wallet.DebitWalletCommand;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/{userId}/credit")
    public ResponseEntity<WalletResponse> creditWallet(
            @PathVariable UUID userId, @RequestBody CreditWalletCommand command) {
        WalletResponse response = walletService.creditWallet(command);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{userId}/debit")
    public ResponseEntity<WalletResponse> debitWallet(
            @PathVariable UUID userId, @RequestBody DebitWalletCommand command) {
        WalletResponse response = walletService.debitWallet(command);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable UUID userId) {
        BigDecimal balance = walletService.getWalletBalance(userId);
        return new ResponseEntity<>(balance, HttpStatus.OK);
    }
}

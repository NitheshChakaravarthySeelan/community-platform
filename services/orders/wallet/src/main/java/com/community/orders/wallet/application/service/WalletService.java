package com.community.orders.wallet.application.service;

import com.community.orders.wallet.domain.model.Wallet;
import com.community.orders.wallet.domain.model.WalletTransaction;
import com.community.orders.wallet.domain.repository.WalletRepository;
import com.community.orders.wallet.domain.repository.WalletTransactionRepository;
import com.community.orders.wallet.interfaces.dto.WalletRequest;
import com.community.orders.wallet.interfaces.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Transactional
    public WalletResponse creditWallet(UUID userId, WalletRequest request) {
        Wallet wallet = walletRepository.findById(userId)
                .orElseGet(() -> createNewWallet(userId)); // Create if not exists

        wallet.credit(request.getAmount());
        Wallet updatedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = recordTransaction(userId, request, "CREDIT", "SUCCESS", updatedWallet.getBalance());
        return mapToResponse(updatedWallet, transaction);
    }

    @Transactional
    public WalletResponse debitWallet(UUID userId, WalletRequest request) {
        Wallet wallet = walletRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user ID: " + userId));

        try {
            wallet.debit(request.getAmount());
            Wallet updatedWallet = walletRepository.save(wallet);
            WalletTransaction transaction = recordTransaction(userId, request, "DEBIT", "SUCCESS", updatedWallet.getBalance());
            return mapToResponse(updatedWallet, transaction);
        } catch (IllegalArgumentException e) {
            WalletTransaction transaction = recordTransaction(userId, request, "DEBIT", "FAILED", wallet.getBalance());
            transaction.setMessage(e.getMessage());
            walletTransactionRepository.save(transaction); // Save failed transaction
            throw e; // Re-throw to inform controller
        }
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletBalance(UUID userId) {
        Wallet wallet = walletRepository.findById(userId)
                .orElseGet(() -> createNewWallet(userId)); // Create if not exists, for consistency

        // For read operations, we don't create a transaction record unless specified
        return new WalletResponse(
                null, // No specific transaction ID for a balance inquiry
                wallet.getUserId(),
                BigDecimal.ZERO, // No amount for a balance inquiry
                wallet.getBalance(),
                null, // No type for a balance inquiry
                "SUCCESS",
                "Current balance",
                Instant.now(),
                null // No reference ID
        );
    }

    private Wallet createNewWallet(UUID userId) {
        Wallet newWallet = Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .build();
        return walletRepository.save(newWallet);
    }

    private WalletTransaction recordTransaction(UUID userId, WalletRequest request, String type, String status, BigDecimal newBalance) {
        WalletTransaction transaction = WalletTransaction.builder()
                .userId(userId)
                .amount(request.getAmount())
                .transactionType(type)
                .status(status)
                .message(String.format("%s of %s %s. New balance: %s", type, request.getAmount(), status, newBalance))
                .timestamp(Instant.now())
                .referenceId(request.getReferenceId())
                .build();
        return walletTransactionRepository.save(transaction);
    }

    private WalletResponse mapToResponse(Wallet wallet, WalletTransaction transaction) {
        return new WalletResponse(
                transaction.getTransactionId(),
                wallet.getUserId(),
                transaction.getAmount(),
                wallet.getBalance(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getMessage(),
                transaction.getTimestamp(),
                transaction.getReferenceId()
        );
    }
}

package com.community.orders.wallet.application.service;

import com.community.orders.wallet.domain.model.Wallet;
import com.community.orders.wallet.domain.model.WalletTransaction;
import com.community.orders.wallet.domain.repository.WalletRepository;
import com.community.orders.wallet.domain.repository.WalletTransactionRepository;
import com.community.orders.wallet.interfaces.dto.WalletResponse;
import com.community.platform.shared.proto.wallet.CreditWalletCommand;
import com.community.platform.shared.proto.wallet.DebitWalletCommand;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Transactional
    public WalletResponse debitWallet(DebitWalletCommand command) {
        String sagaId = command.getMetadata().getSagaId();
        UUID userId = UUID.fromString(command.getUserId());

        Optional<WalletTransaction> existing = walletTransactionRepository.findBySagaId(sagaId);
        if (existing.isPresent()) {
            Wallet wallet = walletRepository.findById(userId).get();
            return mapToResponse(wallet, existing.get());
        }

        Wallet wallet =
                walletRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Wallet not found for user ID: " + userId));

        BigDecimal amount =
                BigDecimal.valueOf(command.getAmountCents()).divide(BigDecimal.valueOf(100));

        try {
            wallet.debit(amount);
            Wallet updatedWallet = walletRepository.save(wallet);
            WalletTransaction transaction =
                    recordTransaction(
                            userId,
                            sagaId,
                            amount,
                            "DEBIT",
                            "SUCCESS",
                            updatedWallet.getBalance(),
                            null);
            return mapToResponse(updatedWallet, transaction);
        } catch (IllegalArgumentException e) {
            recordTransaction(
                    userId, sagaId, amount, "DEBIT", "FAILED", wallet.getBalance(), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public WalletResponse creditWallet(CreditWalletCommand command) {
        String sagaId = command.getMetadata().getSagaId();
        UUID userId = UUID.fromString(command.getUserId());

        Optional<WalletTransaction> existing = walletTransactionRepository.findBySagaId(sagaId);
        if (existing.isPresent()) {
            Wallet wallet = walletRepository.findById(userId).get();
            return mapToResponse(wallet, existing.get());
        }

        Wallet wallet = walletRepository.findById(userId).orElseGet(() -> createNewWallet(userId));

        BigDecimal amount =
                BigDecimal.valueOf(command.getAmountCents()).divide(BigDecimal.valueOf(100));
        wallet.credit(amount);
        Wallet updatedWallet = walletRepository.save(wallet);

        WalletTransaction transaction =
                recordTransaction(
                        userId,
                        sagaId,
                        amount,
                        "CREDIT",
                        "SUCCESS",
                        updatedWallet.getBalance(),
                        null);
        return mapToResponse(updatedWallet, transaction);
    }

    @Transactional(readOnly = true)
    public BigDecimal getWalletBalance(UUID userId) {
        return walletRepository.findById(userId).map(Wallet::getBalance).orElse(BigDecimal.ZERO);
    }

    private Wallet createNewWallet(UUID userId) {
        Wallet newWallet = Wallet.builder().userId(userId).balance(BigDecimal.ZERO).build();
        return walletRepository.save(newWallet);
    }

    private WalletTransaction recordTransaction(
            UUID userId,
            String sagaId,
            BigDecimal amount,
            String type,
            String status,
            BigDecimal newBalance,
            String message) {
        String msg =
                (message != null)
                        ? message
                        : String.format(
                                "%s of %s %s. New balance: %s", type, amount, status, newBalance);
        WalletTransaction transaction =
                WalletTransaction.builder()
                        .userId(userId)
                        .sagaId(sagaId)
                        .amount(amount)
                        .transactionType(type)
                        .status(status)
                        .message(msg)
                        .timestamp(Instant.now())
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
                null);
    }
}

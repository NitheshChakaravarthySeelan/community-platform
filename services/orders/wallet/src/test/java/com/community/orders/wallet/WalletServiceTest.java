package com.community.orders.wallet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.community.orders.wallet.application.service.WalletService;
import com.community.orders.wallet.domain.model.Wallet;
import com.community.orders.wallet.domain.repository.WalletRepository;
import com.community.orders.wallet.domain.repository.WalletTransactionRepository;
import com.community.orders.wallet.interfaces.dto.WalletResponse;
import com.community.platform.shared.proto.common.SagaMetadata;
import com.community.platform.shared.proto.wallet.DebitWalletCommand;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class WalletServiceTest {

    private WalletService walletService;

    @Mock private WalletRepository walletRepository;

    @Mock private WalletTransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        walletService = new WalletService(walletRepository, transactionRepository);
    }

    @Test
    void testDebitWallet_Success() {
        UUID userId = UUID.randomUUID();
        String sagaId = "wallet-saga-1";
        DebitWalletCommand command =
                DebitWalletCommand.newBuilder()
                        .setMetadata(SagaMetadata.newBuilder().setSagaId(sagaId).build())
                        .setUserId(userId.toString())
                        .setAmountCents(1000) // 10.00
                        .build();

        Wallet wallet = new Wallet(userId, new BigDecimal("50.00"));

        when(transactionRepository.findBySagaId(sagaId)).thenReturn(Optional.empty());
        when(walletRepository.findById(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        WalletResponse response = walletService.debitWallet(command);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("40.00"), response.getNewBalance());
        verify(walletRepository).save(any());
    }

    @Test
    void testDebitWallet_InsufficientFunds() {
        UUID userId = UUID.randomUUID();
        String sagaId = "wallet-saga-fail";
        DebitWalletCommand command =
                DebitWalletCommand.newBuilder()
                        .setMetadata(SagaMetadata.newBuilder().setSagaId(sagaId).build())
                        .setUserId(userId.toString())
                        .setAmountCents(10000) // 100.00
                        .build();

        Wallet wallet = new Wallet(userId, new BigDecimal("50.00"));

        when(transactionRepository.findBySagaId(sagaId)).thenReturn(Optional.empty());
        when(walletRepository.findById(userId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThrows(IllegalArgumentException.class, () -> walletService.debitWallet(command));
        verify(transactionRepository).save(argThat(t -> "FAILED".equals(t.getStatus())));
    }
}

package com.community.orders.wallet.domain.repository;

import com.community.orders.wallet.domain.model.WalletTransaction;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    Optional<WalletTransaction> findBySagaId(String sagaId);
}

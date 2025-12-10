package com.community.orders.wallet.domain.repository;

import com.community.orders.wallet.domain.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    // findById (which is findByUserId here) is already provided by JpaRepository
}

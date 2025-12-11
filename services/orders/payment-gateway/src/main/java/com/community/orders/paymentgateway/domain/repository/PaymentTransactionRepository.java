package com.community.orders.paymentgateway.domain.repository;

import com.community.orders.paymentgateway.domain.model.PaymentTransaction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {}

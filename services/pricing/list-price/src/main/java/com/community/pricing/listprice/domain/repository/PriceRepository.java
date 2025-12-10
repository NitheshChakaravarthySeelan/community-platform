package com.community.pricing.listprice.domain.repository;

import com.community.pricing.listprice.domain.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PriceRepository extends JpaRepository<Price, UUID> {
    Optional<Price> findByProductId(UUID productId);
}

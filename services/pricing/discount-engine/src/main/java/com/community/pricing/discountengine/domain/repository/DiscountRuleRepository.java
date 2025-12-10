package com.community.pricing.discountengine.domain.repository;

import com.community.pricing.discountengine.domain.model.DiscountRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiscountRuleRepository extends JpaRepository<DiscountRule, UUID> {
    Optional<DiscountRule> findByCode(String code);
}

package com.community.pricing.taxcalculation.domain.repository;

import com.community.pricing.taxcalculation.domain.model.TaxRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaxRuleRepository extends JpaRepository<TaxRule, UUID> {
    Optional<TaxRule> findByStateAndCountryAndCategory(String state, String country, String category);
    Optional<TaxRule> findByStateAndCountry(String state, String country);
}

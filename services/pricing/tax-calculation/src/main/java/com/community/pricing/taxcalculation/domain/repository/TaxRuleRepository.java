package com.community.pricing.taxcalculation.domain.repository;

import com.community.pricing.taxcalculation.domain.model.TaxRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxRuleRepository extends JpaRepository<TaxRule, Long> {

    // Find a state-specific rule first
    Optional<TaxRule> findByCountryAndState(String country, String state);

    // Fallback to a country-wide rule
    Optional<TaxRule> findByCountryAndStateIsNull(String country);
}

package com.community.pricing.taxcalculation.application.service;

import com.community.pricing.taxcalculation.domain.model.TaxRule;
import com.community.pricing.taxcalculation.domain.repository.TaxRuleRepository;
import com.community.pricing.taxcalculation.interfaces.dto.TaxRequest;
import com.community.pricing.taxcalculation.interfaces.dto.TaxResponse;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaxService {

    private final TaxRuleRepository taxRuleRepository;

    @Transactional(readOnly = true)
    public TaxResponse calculateTax(TaxRequest request) {
        long totalTaxCents = 0;
        TaxRequest.AddressDTO address = request.getShippingAddress();

        if (address != null) {
            // Try to find a state-specific rule first, then fall back to a country-wide rule.
            Optional<TaxRule> rule =
                    taxRuleRepository
                            .findByCountryAndState(address.getCountry(), address.getState())
                            .or(
                                    () ->
                                            taxRuleRepository.findByCountryAndStateIsNull(
                                                    address.getCountry()));

            if (rule.isPresent()) {
                TaxRule taxRule = rule.get();
                BigDecimal taxRate = taxRule.getTaxRatePercentage().divide(BigDecimal.valueOf(100));
                long totalCartValueCents =
                        request.getItems().stream()
                                .mapToLong(item -> item.getPriceCents() * item.getQuantity())
                                .sum();

                BigDecimal totalTax = BigDecimal.valueOf(totalCartValueCents).multiply(taxRate);
                totalTaxCents = totalTax.longValue();
            }
        }
        // If no address or no rule, tax is 0.
        return new TaxResponse(totalTaxCents);
    }
}

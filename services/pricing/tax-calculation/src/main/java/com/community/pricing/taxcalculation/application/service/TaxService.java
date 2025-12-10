package com.community.pricing.taxcalculation.application.service;

import com.community.pricing.taxcalculation.domain.model.TaxRule;
import com.community.pricing.taxcalculation.domain.repository.TaxRuleRepository;
import com.community.pricing.taxcalculation.interfaces.dto.TaxRequest;
import com.community.pricing.taxcalculation.interfaces.dto.TaxResponse;
import com.community.pricing.taxcalculation.interfaces.dto.TaxItemResponseDTO;
import com.community.pricing.taxcalculation.interfaces.dto.TaxItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaxService {

    private final TaxRuleRepository taxRuleRepository;
    private static final int DECIMAL_PLACES = 2; // For currency calculations

    @Transactional(readOnly = true)
    public TaxResponse calculateTax(TaxRequest request) {
        List<TaxItemResponseDTO> itemResponses = new ArrayList<>();
        BigDecimal totalNetPrice = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal totalGrossPrice = BigDecimal.ZERO;

        String state = request.getDestinationAddress().getState();
        String country = request.getDestinationAddress().getCountry();

        for (TaxItemDTO item : request.getItems()) {
            BigDecimal netPrice = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal taxRate = getTaxRate(state, country, item.getProductId().toString()); // Assuming productId can map to category for now
            BigDecimal taxAmount = netPrice.multiply(taxRate).setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
            BigDecimal grossPrice = netPrice.add(taxAmount).setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);

            itemResponses.add(new TaxItemResponseDTO(
                    item.getProductId(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    netPrice.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP),
                    taxRate,
                    taxAmount,
                    grossPrice
            ));

            totalNetPrice = totalNetPrice.add(netPrice);
            totalTaxAmount = totalTaxAmount.add(taxAmount);
            totalGrossPrice = totalGrossPrice.add(grossPrice);
        }

        return new TaxResponse(
                itemResponses,
                totalNetPrice.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP),
                totalTaxAmount.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP),
                totalGrossPrice.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP),
                "USD" // Assuming default currency
        );
    }

    private BigDecimal getTaxRate(String state, String country, String category) {
        // Prioritize category-specific tax rules
        Optional<TaxRule> rule = taxRuleRepository.findByStateAndCountryAndCategory(state, country, category);
        if (rule.isPresent()) {
            return rule.get().getTaxRate();
        }

        // Fallback to general state/country tax rules
        rule = taxRuleRepository.findByStateAndCountry(state, country);
        if (rule.isPresent()) {
            return rule.get().getTaxRate();
        }

        // Default tax rate if no specific rule is found
        return BigDecimal.ZERO;
    }
}

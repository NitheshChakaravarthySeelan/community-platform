package com.community.pricing.discountengine.application.service;

import com.community.pricing.discountengine.domain.model.DiscountRule;
import com.community.pricing.discountengine.domain.repository.DiscountRuleRepository;
import com.community.pricing.discountengine.interfaces.dto.DiscountRequest;
import com.community.pricing.discountengine.interfaces.dto.DiscountResponse;
import com.community.pricing.discountengine.interfaces.dto.DiscountedProductDTO;
import com.community.pricing.discountengine.interfaces.dto.ProductPriceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRuleRepository discountRuleRepository;

    @Transactional(readOnly = true)
    public DiscountResponse calculateDiscounts(DiscountRequest request) {
        List<DiscountedProductDTO> discountedProducts = new ArrayList<>();
        BigDecimal totalOriginalAmount = BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        String appliedCouponCode = null;

        // Fetch applicable rules (e.g., global rules, coupon-specific rules)
        List<DiscountRule> applicableRules = new ArrayList<>();
        if (request.getCouponCode() != null && !request.getCouponCode().isEmpty()) {
            Optional<DiscountRule> couponRule = discountRuleRepository.findByCode(request.getCouponCode());
            couponRule.filter(DiscountRule::isActive)
                      .filter(rule -> rule.getExpirationDate() == null || rule.getExpirationDate().isAfter(Instant.now()))
                      .ifPresent(applicableRules::add);
            if (couponRule.isPresent()) {
                appliedCouponCode = couponRule.get().getCode();
            }
        }
        // Add other global or automatic discounts here if any
        // For simplicity, only coupon rules are considered for now

        for (ProductPriceDTO productDTO : request.getProducts()) {
            BigDecimal originalPrice = productDTO.getPrice().multiply(BigDecimal.valueOf(productDTO.getQuantity()));
            totalOriginalAmount = totalOriginalAmount.add(originalPrice);
            BigDecimal currentPrice = originalPrice;
            BigDecimal discountForProduct = BigDecimal.ZERO;
            String ruleApplied = null;

            // Apply rules to each product or the entire cart
            // This is a simplified logic, real-world engines are more complex
            for (DiscountRule rule : applicableRules) {
                if (rule.getType().equals("PERCENT_OFF") && rule.getValue() != null) {
                    BigDecimal discountFactor = rule.getValue(); // e.g., 0.20 for 20%
                    BigDecimal calculatedDiscount = currentPrice.multiply(discountFactor);
                    currentPrice = currentPrice.subtract(calculatedDiscount);
                    discountForProduct = discountForProduct.add(calculatedDiscount);
                    ruleApplied = rule.getCode();
                    break; // Apply only one coupon rule per product for simplicity
                }
                // Add logic for AMOUNT_OFF, BOGO, etc.
            }

            discountedProducts.add(new DiscountedProductDTO(
                    productDTO.getProductId(),
                    originalPrice,
                    currentPrice,
                    discountForProduct,
                    ruleApplied
            ));
            totalDiscountAmount = totalDiscountAmount.add(discountForProduct);
        }

        BigDecimal totalDiscountedAmount = totalOriginalAmount.subtract(totalDiscountAmount);

        return new DiscountResponse(
                discountedProducts,
                totalOriginalAmount,
                totalDiscountedAmount,
                totalDiscountAmount,
                appliedCouponCode,
                "Discounts calculated successfully."
        );
    }
}

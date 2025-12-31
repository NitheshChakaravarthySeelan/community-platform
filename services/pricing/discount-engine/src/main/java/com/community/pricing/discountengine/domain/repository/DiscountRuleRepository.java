package com.community.pricing.discountengine.domain.repository;

import com.community.pricing.discountengine.domain.model.DiscountRule;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountRuleRepository extends JpaRepository<DiscountRule, Long> {

    List<DiscountRule> findByProductIdAndActiveTrueAndStartDateBeforeAndEndDateAfter(
            String productId, Instant now, Instant alsoNow);

    List<DiscountRule> findByCouponCodeAndActiveTrueAndStartDateBeforeAndEndDateAfter(
            String couponCode, Instant now, Instant alsoNow);
}

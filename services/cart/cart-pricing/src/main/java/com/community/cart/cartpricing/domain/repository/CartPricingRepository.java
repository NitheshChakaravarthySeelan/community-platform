package com.community.cart.cartpricing;

import com.community.cart.cartpricing.domain.model.CartPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartPricingCatalog implements JpaRepository<CartPrice, Long> {

}

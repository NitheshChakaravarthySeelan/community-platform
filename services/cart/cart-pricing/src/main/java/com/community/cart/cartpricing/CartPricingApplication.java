package com.community.cart.cartpricing;

import com.community.cart.cartpricing.config.CartPricingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CartPricingConfig.class)
public class CartPricingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartPricingApplication.class, args);
    }
}

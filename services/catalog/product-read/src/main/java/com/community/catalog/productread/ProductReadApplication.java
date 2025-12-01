package com.community.catalog.productread;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProductReadApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductReadApplication.class, args);
    }

}

package com.community.catalog.productwrite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProductWriteApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductWriteApplication.class, args);
    }
}


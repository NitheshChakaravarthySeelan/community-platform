package com.community.catalog.productread.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import product_lookup.ProductLookupGrpc;

@Configuration
public class GrpcClientConfig {

    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
    }

    @Bean
    public ProductLookupGrpc.ProductLookupBlockingStub productLookupBlockingStub(ManagedChannel managedChannel) {
        return ProductLookupGrpc.newBlockingStub(managedChannel);
    }
}

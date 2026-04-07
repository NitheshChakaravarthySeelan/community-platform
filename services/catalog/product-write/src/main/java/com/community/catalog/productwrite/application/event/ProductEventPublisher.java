package com.community.catalog.productwrite.application.event;

import com.community.catalog.productwrite.domain.model.Product;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.community.catalog.events.CatalogEventsProto.ProductUpdatedEvent;
import com.community.catalog.events.CatalogEventsProto.ProductCreatedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventPublisher {

    @Value("${product.events.topic}")
    private String productEventsTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishProductCreatedEvent(Product product) {
        ProductCreatedEvent event = ProductCreatedEvent.newBuilder()
            .setProductId(product.getId().toString())
            .setSku(product.getSku())
            .setName(product.getName())
            .setQuantity(product.getStockQuantity())
            .build();

        byte[] payload = event.toByteArray();

        ProducerRecord<String, Object> record = new ProducerRecord<String,Object>(productEventsTopic, product.getId().toString(), payload);
        record.headers().add(new RecordHeader("event_type", "ProductCreated".getBytes()));

        kafkaTemplate.send(record);
        log.info("Published ProductCreatedEvent for Product ID: {}", product.getId().toString());
    }

    public void publishProductUpdatedEvent(Product product) {
        ProductUpdatedEvent event = ProductUpdatedEvent.newBuilder()
            .setProductId(product.getId().toString())
            .setName(product.getName())
            .setDescription(product.getDescription())
            .setPrice(product.getPrice())
            .setSku(product.getSku())
            .setImageUrl(product.getImageUrl())
            .setCategory(product.getCategory())
            .setManufacturer(product.getManufacturer())
            .setQuantity(product.getStockQuantity())
            .setStatus(product.getStatus())
            .setUpdatedAt(product.getUpdatedAt().toString())
            .build();

        byte[] payload = event.toByteArray();

        ProducerRecord<String, Object> record = new ProducerRecord<>(productEventsTopic, product.getId().toString(), payload);
        record.headers().add(new RecordHeader("event_type", "ProductUpdated".getBytes()));


        kafkaTemplate.send(record);
        log.info("Published ProductUpdatedEvent for Product ID: {}", product.getId().toString());
    }
}
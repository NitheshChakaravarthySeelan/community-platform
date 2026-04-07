package com.community.catalog.productread.interfaces.kafka;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.community.catalog.events.CatalogEventsProto.ProductCreatedEvent;
import com.community.catalog.events.CatalogEventsProto.ProductUpdatedEvent;
import com.community.catalog.productread.domain.model.ProductView;
import com.community.catalog.productread.domain.repository.ProductViewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

    private final ProductViewRepository productViewRepository;

    @KafkaListener(topics = "${product.events.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, byte[]> record) {
        try {
            String eventTypeHeader = new String(record.headers().lastHeader("event_type").value());
            log.info("Received Kafka event: {} with key: {}", eventTypeHeader, record.key());

            if ("ProductCreated".equals(eventTypeHeader)) {
                handleProductCreated(ProductCreatedEvent.parseFrom(record.value()));
            } else if ("ProductUpdated".equals(eventTypeHeader)) {
                handleProductUpdated(ProductUpdatedEvent.parseFrom(record.value()));
            } else {
                log.warn("Unknown event type: {}", eventTypeHeader);
            }
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", e.getMessage(), e);
        }
    }

    private void handleProductCreated(ProductCreatedEvent event) {
        ProductView productView = ProductView.builder()
                .id(UUID.fromString(event.getProductId()))
                .name(event.getName())
                .description(event.getDescription())
                .price(BigDecimal.valueOf(event.getPrice()))
                .quantity(event.getQuantity())
                .sku(event.getSku())
                .imageUrl(event.getImageUrl())
                .category(event.getCategory())
                .manufacturer(event.getManufacturer())
                .status(event.getStatus())
                .version((int) event.getVersion())
                .createdAt(new Date()) // Or parse from event if available
                .updatedAt(new Date())
                .build();

        productViewRepository.save(productView);
        log.info("Successfully created ProductView for ID: {}", event.getProductId());
    }

    private void handleProductUpdated(ProductUpdatedEvent event) {
        UUID productId = UUID.fromString(event.getProductId());
        
        ProductView productView = ProductView.builder()
                .id(productId)
                .name(event.getName())
                .description(event.getDescription())
                .price(BigDecimal.valueOf(event.getPrice()))
                .quantity(event.getQuantity())
                .sku(event.getSku())
                .imageUrl(event.getImageUrl())
                .category(event.getCategory())
                .manufacturer(event.getManufacturer())
                .status(event.getStatus())
                .version((int) event.getVersion())
                .updatedAt(new Date()) // Update the timestamp
                .createdAt(new Date()) // Ideally we'd keep the original createdAt
                .build();

        productViewRepository.save(productView);
        log.info("Successfully updated ProductView for ID: {}", event.getProductId());
    }
}

package com.community.catalog.productread.interfaces.kafka;

import com.community.catalog.events.CatalogEventsProto.ProductCreatedEvent;
import com.community.catalog.events.CatalogEventsProto.ProductUpdatedEvent;
import com.community.catalog.productread.domain.model.ProductView;
import com.community.catalog.productread.domain.repository.ProductViewRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductEventListenerTest {

    @Mock
    private ProductViewRepository productViewRepository;

    @InjectMocks
    private ProductEventListener productEventListener;

    @Test
    void testListen_WhenProductCreatedEventReceived_ShouldSaveProductView() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();
        ProductCreatedEvent event = ProductCreatedEvent.newBuilder()
                .setProductId(productId.toString())
                .setSku("SKU-123")
                .setName("Test Product")
                .setQuantity(10)
                .setPrice(99.99)
                .build();
        byte[] payload = event.toByteArray();

        RecordHeaders headers = new RecordHeaders();
        headers.add(new RecordHeader("event_type", "ProductCreated".getBytes()));

        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("topic", 0, 0, productId.toString(), payload);
        // Add headers to record
        for (org.apache.kafka.common.header.Header h : headers) {
            record.headers().add(h);
        }

        // Act
        productEventListener.listen(record);

        // Assert
        ArgumentCaptor<ProductView> captor = ArgumentCaptor.forClass(ProductView.class);
        verify(productViewRepository).save(captor.capture());
        ProductView savedView = captor.getValue();

        assertEquals(productId, savedView.getId());
        assertEquals("Test Product", savedView.getName());
        assertEquals("SKU-123", savedView.getSku());
    }

    @Test
    void testListen_WhenProductUpdatedEventReceived_ShouldSaveProductView() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();
        ProductUpdatedEvent event = ProductUpdatedEvent.newBuilder()
                .setProductId(productId.toString())
                .setName("Updated Product")
                .setPrice(149.99)
                .setQuantity(20)
                .setSku("SKU-123")
                .setStatus("ACTIVE")
                .build();
        byte[] payload = event.toByteArray();

        RecordHeaders headers = new RecordHeaders();
        headers.add(new RecordHeader("event_type", "ProductUpdated".getBytes()));

        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("topic", 0, 0, productId.toString(), payload);
        for (org.apache.kafka.common.header.Header h : headers) {
            record.headers().add(h);
        }

        // Act
        productEventListener.listen(record);

        // Assert
        ArgumentCaptor<ProductView> captor = ArgumentCaptor.forClass(ProductView.class);
        verify(productViewRepository).save(captor.capture());
        ProductView savedView = captor.getValue();

        assertEquals(productId, savedView.getId());
        assertEquals("Updated Product", savedView.getName());
        assertEquals(149.99, savedView.getPrice().doubleValue());
    }
}

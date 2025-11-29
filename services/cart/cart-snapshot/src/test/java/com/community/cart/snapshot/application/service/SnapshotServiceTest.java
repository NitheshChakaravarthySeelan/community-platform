package com.community.cart.snapshot.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.community.cart.snapshot.domain.model.UserCartSnapshot;
import com.community.cart.snapshot.domain.repository.UserCartSnapshotRepository;
import com.community.cart.snapshot.infrastructure.client.CartCrudAdapter;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SnapshotServiceTest {

    @Mock private UserCartSnapshotRepository snapshotRepository;

    @Mock private CartCrudAdapter cartCrudAdapter;

    @InjectMocks private SnapshotService snapshotService;

    private Long userId;
    private String nonEmptyCartJson;

    @BeforeEach
    void setUp() {
        userId = 123L;
        nonEmptyCartJson = "{\"items\":[{\"productId\":1,\"quantity\":2}]}";
    }

    @Test
    void createSnapshot_shouldSucceed_whenCartIsNotEmpty() {
        // ARRANGE
        when(cartCrudAdapter.getLiveCart(userId)).thenReturn(nonEmptyCartJson);

        when(snapshotRepository.save(any(UserCartSnapshot.class)))
                .thenAnswer(
                        invocation -> {
                            UserCartSnapshot snapshotToSave = invocation.getArgument(0);
                            snapshotToSave.setId(UUID.randomUUID());
                            return snapshotToSave;
                        });

        // ACT
        UserCartSnapshot createdSnapshot = snapshotService.createSnapshot(userId);

        // ASSERT
        assertNotNull(createdSnapshot);
        assertNotNull(createdSnapshot.getId());
        assertEquals(userId, createdSnapshot.getUserId());
        assertEquals(nonEmptyCartJson, createdSnapshot.getItems());

        verify(cartCrudAdapter, times(1)).getLiveCart(userId);
        verify(snapshotRepository, times(1)).save(any(UserCartSnapshot.class));
    }

    @Test
    void createSnapshot_shouldThrowException_whenCartIsEmpty() {
        // ARRANGE
        when(cartCrudAdapter.getLiveCart(userId)).thenReturn("[]");

        // ACT & ASSERT
        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class, () -> snapshotService.createSnapshot(userId));

        assertEquals(
                "Cannot create a snapshot of an empty or non-existent cart.",
                exception.getMessage());

        verify(snapshotRepository, never()).save(any(UserCartSnapshot.class));
    }

    @Test
    void createSnapshot_shouldThrowException_whenCartServiceIsDown() {
        // ARRANGE
        when(cartCrudAdapter.getLiveCart(userId))
                .thenThrow(
                        new IllegalStateException("Could not fetch live cart for user: " + userId));

        // ACT & ASSERT
        assertThrows(IllegalStateException.class, () -> snapshotService.createSnapshot(userId));

        verify(snapshotRepository, never()).save(any(UserCartSnapshot.class));
    }
}

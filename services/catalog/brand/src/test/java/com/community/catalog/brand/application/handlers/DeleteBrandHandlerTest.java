package com.community.catalog.brand.application.handlers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.community.catalog.brand.application.commands.DeleteBrandCommand;
import com.community.catalog.brand.domain.repository.BrandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteBrandHandlerTest {

    @Mock private BrandRepository brandRepository;

    @InjectMocks private DeleteBrandCommandHandler commandHandler;

    @Test
    void testDeleteBrandSuccess() {
        // Arrange
        var command = new DeleteBrandCommand(1L);
        when(brandRepository.existsById(1L)).thenReturn(true);
        doNothing().when(brandRepository).deleteById(1L);

        // Act
        commandHandler.handle(command);

        // Assert
        verify(brandRepository).existsById(1L);
        verify(brandRepository).deleteById(1L);
    }

    @Test
    void testDeleteBrandNotFound() {
        // Arrange
        var command = new DeleteBrandCommand(1L);
        when(brandRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> commandHandler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Brand not found with id: 1");
        verify(brandRepository, never()).deleteById(1L);
    }
}

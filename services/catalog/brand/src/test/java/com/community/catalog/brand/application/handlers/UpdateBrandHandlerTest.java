package com.community.catalog.brand.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.community.catalog.brand.application.commands.UpdateBrandCommand;
import com.community.catalog.brand.domain.model.Brand;
import com.community.catalog.brand.domain.repository.BrandRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateBrandHandlerTest {

    @Mock private BrandRepository brandRepository;

    @InjectMocks private UpdateBrandCommandHandler commandHandler;

    @Test
    void testUpdateBrandSuccess() {
        // Arrange
        var command = new UpdateBrandCommand(1L, "Updated Name", "Updated Desc");
        var existingBrand = new Brand("Old Name", "Old Desc");
        existingBrand.setId(1L);

        when(brandRepository.findById(1L)).thenReturn(Optional.of(existingBrand));
        when(brandRepository.save(existingBrand)).thenReturn(existingBrand);

        // Act
        Brand result = commandHandler.handle(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isEqualTo("Updated Desc");
        verify(brandRepository).findById(1L);
        verify(brandRepository).save(existingBrand);
    }

    @Test
    void testUpdateBrandNotFound() {
        // Arrange
        var command = new UpdateBrandCommand(1L, "Updated Name", "Updated Desc");
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> commandHandler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Brand not found with id: 1");
    }
}

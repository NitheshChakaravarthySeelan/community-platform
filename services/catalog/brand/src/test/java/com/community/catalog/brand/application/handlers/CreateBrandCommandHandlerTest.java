package com.community.catalog.brand.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.community.catalog.brand.application.commands.CreateBrandCommand;
import com.community.catalog.brand.domain.model.Brand;
import com.community.catalog.brand.domain.repository.BrandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateBrandCommandHandlerTest {

    @Mock private BrandRepository brandRepository;

    @InjectMocks private CreateBrandCommandHandler commandHandler;

    @Test
    void should_create_brand_successfully_when_given_valid_command() {
        // Arrange
        var command = new CreateBrandCommand("Nike", "Just Do It");
        var brandToSave = new Brand(command.name(), command.description());

        // We use ArgumentCaptor to capture the argument passed to the save method
        var brandCaptor = ArgumentCaptor.forClass(Brand.class);

        when(brandRepository.save(brandCaptor.capture())).thenReturn(brandToSave);

        // Act
        Brand result = commandHandler.handle(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(command.name());
        assertThat(result.getDescription()).isEqualTo(command.description());

        // Verify the captured argument
        Brand capturedBrand = brandCaptor.getValue();
        assertThat(capturedBrand.getName()).isEqualTo("Nike");

        // Verify that the save method was called exactly once
        verify(brandRepository).save(any(Brand.class));
    }

    @Test
    void should_throw_exception_when_brand_name_is_null() {
        // Arrange
        var command = new CreateBrandCommand(null, "A brand with no name");

        // Act & Assert
        assertThatThrownBy(() -> commandHandler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Brand name cannot be null or empty.");

        // Verify that the save method was never called
        verify(brandRepository, never()).save(any(Brand.class));
    }
}

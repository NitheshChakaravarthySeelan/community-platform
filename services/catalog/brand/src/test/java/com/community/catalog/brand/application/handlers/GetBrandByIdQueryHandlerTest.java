package com.community.catalog.brand.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.community.catalog.brand.application.queries.GetBrandByIdQuery;
import com.community.catalog.brand.domain.model.Brand;
import com.community.catalog.brand.domain.repository.BrandRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetBrandByIdQueryHandlerTest {

    @Mock private BrandRepository brandRepository;

    @InjectMocks private GetBrandByIdQueryHandler queryHandler;

    @Test
    void testGetBrandByIdSuccess() {
        // Arrange
        var query = new GetBrandByIdQuery(1L);
        var brand = new Brand("Test Brand", "Test Desc");
        brand.setId(1L);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        // Act
        Optional<Brand> result = queryHandler.handle(query);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void testGetBrandByIdNotFound() {
        // Arrange
        var query = new GetBrandByIdQuery(1L);
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Brand> result = queryHandler.handle(query);

        // Assert
        assertThat(result).isNotPresent();
    }
}

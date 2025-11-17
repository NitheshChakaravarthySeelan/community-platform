package com.community.catalog.brand.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.community.catalog.brand.application.queries.GetAllBrandsQuery;
import com.community.catalog.brand.domain.model.Brand;
import com.community.catalog.brand.domain.repository.BrandRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAllBrandsQueryHandlerTest {

    @Mock private BrandRepository brandRepository;

    @InjectMocks private GetAllBrandsQueryHandler queryHandler;

    @Test
    void testGetAllBrands() {
        // Arrange
        var query = new GetAllBrandsQuery();
        var brandList = List.of(new Brand("Brand1", "Desc1"), new Brand("Brand2", "Desc2"));
        when(brandRepository.findAll()).thenReturn(brandList);

        // Act
        List<Brand> result = queryHandler.handle(query);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).isEqualTo(brandList);
    }
}

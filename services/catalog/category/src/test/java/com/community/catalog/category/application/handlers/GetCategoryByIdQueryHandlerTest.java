package com.community.catalog.category.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.community.catalog.category.application.queries.GetCategoryByIdQuery;
import com.community.catalog.category.domain.model.Category;
import com.community.catalog.category.domain.repository.CategoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCategoryByIdQueryHandlerTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private GetCategoryByIdQueryHandler queryHandler;

    @Test
    void testGetCategoryById() {
        // Arrange
        var query = new GetCategoryByIdQuery(1L);
        var category = new Category("Test", "Desc");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Act
        Optional<Category> result = queryHandler.handle(query);

        // Assert
        assertThat(result).isPresent();
    }
}

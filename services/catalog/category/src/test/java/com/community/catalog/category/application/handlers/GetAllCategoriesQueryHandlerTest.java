package com.community.catalog.category.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.community.catalog.category.application.queries.GetAllCategoriesQuery;
import com.community.catalog.category.domain.model.Category;
import com.community.catalog.category.domain.repository.CategoryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAllCategoriesQueryHandlerTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private GetAllCategoriesQueryHandler queryHandler;

    @Test
    void testGetAllCategories() {
        // Arrange
        var query = new GetAllCategoriesQuery();
        var categoryList = List.of(new Category("Cat1", "Desc1"), new Category("Cat2", "Desc2"));
        when(categoryRepository.findAll()).thenReturn(categoryList);

        // Act
        List<Category> result = queryHandler.handle(query);

        // Assert
        assertThat(result).hasSize(2);
    }
}

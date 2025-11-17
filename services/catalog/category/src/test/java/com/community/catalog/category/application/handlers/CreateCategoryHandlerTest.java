package com.community.catalog.category.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.community.catalog.category.application.commands.CreateCategoryCommand;
import com.community.catalog.category.domain.model.Category;
import com.community.catalog.category.domain.repository.CategoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateCategoryHandlerTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private CreateCategoryCommandHandler commandHandler;

    @Test
    void testCreateCategoryWithoutParent() {
        // Arrange
        var command = new CreateCategoryCommand("Electronics", "All electronic items", null);
        var category = new Category(command.name(), command.description());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        Category result = commandHandler.handle(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Electronics");
        assertThat(result.getParent()).isNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testCreateCategoryWithParent() {
        // Arrange
        var parentCategory = new Category("Electronics", "Parent");
        parentCategory.setId(1L);
        var command = new CreateCategoryCommand("Laptops", "All laptops", 1L);
        var category = new Category(command.name(), command.description());
        category.setParent(parentCategory);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        Category result = commandHandler.handle(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptops");
        assertThat(result.getParent()).isNotNull();
        assertThat(result.getParent().getId()).isEqualTo(1L);
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }
}

package com.community.catalog.category.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.community.catalog.category.application.commands.UpdateCategoryCommand;
import com.community.catalog.category.domain.model.Category;
import com.community.catalog.category.domain.repository.CategoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateCategoryHandlerTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private UpdateCategoryCommandHandler commandHandler;

    @Test
    void testUpdateCategory() {
        // Arrange
        var command = new UpdateCategoryCommand(1L, "Updated Name", "Updated Desc", null);
        var existingCategory = new Category("Old Name", "Old Desc");
        existingCategory.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(existingCategory)).thenReturn(existingCategory);

        // Act
        Category result = commandHandler.handle(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(existingCategory);
    }
}

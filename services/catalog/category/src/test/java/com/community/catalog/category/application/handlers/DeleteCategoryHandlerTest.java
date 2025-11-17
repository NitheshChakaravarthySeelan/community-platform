package com.community.catalog.category.application.handlers;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.community.catalog.category.application.commands.DeleteCategoryCommand;
import com.community.catalog.category.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteCategoryHandlerTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private DeleteCategoryCommandHandler commandHandler;

    @Test
    void testDeleteCategory() {
        // Arrange
        var command = new DeleteCategoryCommand(1L);
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(1L);

        // Act
        commandHandler.handle(command);

        // Assert
        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).deleteById(1L);
    }
}

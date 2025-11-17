package com.community.catalog.category.application.handlers;

import com.community.catalog.category.application.commands.DeleteCategoryCommand;
import com.community.catalog.category.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteCategoryCommandHandler implements CommandHandler<Void, DeleteCategoryCommand> {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Void handle(DeleteCategoryCommand command) {
        if (!categoryRepository.existsById(command.id())) {
            throw new RuntimeException("Category not found with id: " + command.id());
        }
        categoryRepository.deleteById(command.id());
        return null;
    }
}

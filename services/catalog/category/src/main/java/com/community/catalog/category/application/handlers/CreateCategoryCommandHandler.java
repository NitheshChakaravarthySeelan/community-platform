package com.community.catalog.category.application.handlers;

import com.community.catalog.category.application.commands.CreateCategoryCommand;
import com.community.catalog.category.domain.model.Category;
import com.community.catalog.category.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreateCategoryCommandHandler
        implements CommandHandler<Category, CreateCategoryCommand> {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category handle(CreateCategoryCommand command) {
        Category parent = null;
        if (command.parentId() != null) {
            parent =
                    categoryRepository
                            .findById(command.parentId())
                            .orElseThrow(
                                    () ->
                                            new RuntimeException(
                                                    "Parent category not found with id: "
                                                            + command.parentId()));
        }

        Category category = new Category(command.name(), command.description());
        category.setParent(parent);
        return categoryRepository.save(category);
    }
}

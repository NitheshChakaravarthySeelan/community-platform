package com.community.catalog.category.application.handlers;

import com.community.catalog.category.application.queries.GetCategoryByIdQuery;
import com.community.catalog.category.domain.model.Category;
import com.community.catalog.category.domain.repository.CategoryRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GetCategoryByIdQueryHandler
        implements CommandHandler<Optional<Category>, GetCategoryByIdQuery> {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> handle(GetCategoryByIdQuery query) {
        return categoryRepository.findById(query.id());
    }
}

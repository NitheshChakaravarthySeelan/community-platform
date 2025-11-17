package com.community.catalog.category.application.handlers;

import com.community.catalog.category.application.queries.GetAllCategoriesQuery;
import com.community.catalog.category.domain.model.Category;
import com.community.catalog.category.domain.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GetAllCategoriesQueryHandler
        implements CommandHandler<List<Category>, GetAllCategoriesQuery> {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Category> handle(GetAllCategoriesQuery query) {
        return categoryRepository.findAll();
    }
}

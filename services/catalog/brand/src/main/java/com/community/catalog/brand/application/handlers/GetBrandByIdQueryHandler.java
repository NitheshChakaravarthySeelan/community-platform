package com.community.catalog.brand.application.handlers;

import com.community.catalog.brand.application.queries.GetBrandByIdQuery;
import com.community.catalog.brand.domain.model.Brand;
import com.community.catalog.brand.domain.repository.BrandRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GetBrandByIdQueryHandler
        implements CommandHandler<Optional<Brand>, GetBrandByIdQuery> {

    private final BrandRepository brandRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Brand> handle(GetBrandByIdQuery query) {
        return brandRepository.findById(query.id());
    }
}

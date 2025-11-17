package com.community.catalog.brand.application.handlers;

import com.community.catalog.brand.application.queries.GetAllBrandsQuery;
import com.community.catalog.brand.domain.model.Brand;
import com.community.catalog.brand.domain.repository.BrandRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GetAllBrandsQueryHandler implements CommandHandler<List<Brand>, GetAllBrandsQuery> {

    private final BrandRepository brandRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Brand> handle(GetAllBrandsQuery query) {
        return brandRepository.findAll();
    }
}

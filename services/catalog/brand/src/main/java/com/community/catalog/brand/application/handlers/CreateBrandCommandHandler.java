package com.community.catalog.brand.application.handlers;

import com.community.catalog.brand.application.commands.CreateBrandCommand;
import com.community.catalog.brand.domain.model.Brand;
import com.community.catalog.brand.domain.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreateBrandCommandHandler implements CommandHandler<Brand, CreateBrandCommand> {

    private final BrandRepository brandRepository;

    @Override
    @Transactional
    public Brand handle(CreateBrandCommand command) {
        if (command.name() == null || command.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Brand name cannot be null or empty.");
        }
        Brand brand = new Brand(command.name(), command.description());
        return brandRepository.save(brand);
    }
}

package com.community.catalog.brand.application.handlers;

import com.community.catalog.brand.application.commands.UpdateBrandCommand;
import com.community.catalog.brand.domain.model.Brand;
import com.community.catalog.brand.domain.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateBrandCommandHandler implements CommandHandler<Brand, UpdateBrandCommand> {

    private final BrandRepository brandRepository;

    @Override
    @Transactional
    public Brand handle(UpdateBrandCommand command) {
        return brandRepository
                .findById(command.id())
                .map(
                        existingBrand -> {
                            existingBrand.setName(command.name());
                            existingBrand.setDescription(command.description());
                            return brandRepository.save(existingBrand);
                        })
                .orElseThrow(
                        () -> new RuntimeException("Brand not found with id: " + command.id()));
    }
}

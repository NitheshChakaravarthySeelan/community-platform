package com.community.catalog.brand.application.handlers;

import com.community.catalog.brand.application.commands.DeleteBrandCommand;
import com.community.catalog.brand.domain.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteBrandCommandHandler implements CommandHandler<Void, DeleteBrandCommand> {

    private final BrandRepository brandRepository;

    @Override
    @Transactional
    public Void handle(DeleteBrandCommand command) {
        if (!brandRepository.existsById(command.id())) {
            throw new RuntimeException("Brand not found with id: " + command.id());
        }
        brandRepository.deleteById(command.id());
        return null;
    }
}

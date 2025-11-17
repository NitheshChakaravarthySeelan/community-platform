package com.community.catalog.category.interfaces.controller;

import com.community.catalog.category.application.commands.CreateCategoryCommand;
import com.community.catalog.category.application.commands.DeleteCategoryCommand;
import com.community.catalog.category.application.commands.UpdateCategoryCommand;
import com.community.catalog.category.application.mediator.Mediator;
import com.community.catalog.category.application.queries.GetAllCategoriesQuery;
import com.community.catalog.category.application.queries.GetCategoryByIdQuery;
import com.community.catalog.category.domain.model.Category;
import com.community.catalog.category.interfaces.dto.CategoryResponseDTO;
import com.community.catalog.category.interfaces.dto.CreateCategoryRequestDTO;
import com.community.catalog.category.interfaces.dto.UpdateCategoryRequestDTO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final Mediator mediator;

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CreateCategoryRequestDTO requestDTO) {
        var command =
                new CreateCategoryCommand(
                        requestDTO.name(), requestDTO.description(), requestDTO.parentId());
        Category category = mediator.send(command);
        return new ResponseEntity<>(CategoryResponseDTO.fromEntity(category), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        var query = new GetCategoryByIdQuery(id);
        Optional<Category> category = mediator.send(query);
        return category.map(c -> ResponseEntity.ok(CategoryResponseDTO.fromEntity(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        var query = new GetAllCategoriesQuery();
        List<Category> categories = mediator.send(query);
        List<CategoryResponseDTO> dtos =
                categories.stream()
                        .map(CategoryResponseDTO::fromEntity)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long id, @Valid @RequestBody UpdateCategoryRequestDTO requestDTO) {
        var command =
                new UpdateCategoryCommand(
                        id, requestDTO.name(), requestDTO.description(), requestDTO.parentId());
        Category category = mediator.send(command);
        return ResponseEntity.ok(CategoryResponseDTO.fromEntity(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        var command = new DeleteCategoryCommand(id);
        mediator.send(command);
        return ResponseEntity.noContent().build();
    }
}

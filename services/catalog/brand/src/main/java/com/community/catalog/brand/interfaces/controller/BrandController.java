package com.community.catalog.brand.interfaces.controller;

import com.community.catalog.brand.application.commands.CreateBrandCommand;
import com.community.catalog.brand.application.commands.DeleteBrandCommand;
import com.community.catalog.brand.application.commands.UpdateBrandCommand;
import com.community.catalog.brand.application.mediator.Mediator;
import com.community.catalog.brand.application.queries.GetAllBrandsQuery;
import com.community.catalog.brand.application.queries.GetBrandByIdQuery;
import com.community.catalog.brand.domain.model.Brand;
import com.community.catalog.brand.interfaces.dto.BrandResponseDTO;
import com.community.catalog.brand.interfaces.dto.CreateBrandRequestDTO;
import com.community.catalog.brand.interfaces.dto.UpdateBrandRequestDTO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final Mediator mediator;

    @PostMapping
    public ResponseEntity<BrandResponseDTO> createBrand(
            @Valid @RequestBody CreateBrandRequestDTO requestDTO) {
        var command = new CreateBrandCommand(requestDTO.name(), requestDTO.description());
        Brand brand = mediator.send(command);
        return new ResponseEntity<>(BrandResponseDTO.fromEntity(brand), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponseDTO> getBrandById(@PathVariable Long id) {
        var query = new GetBrandByIdQuery(id);
        Optional<Brand> brand = mediator.send(query);
        return brand.map(b -> ResponseEntity.ok(BrandResponseDTO.fromEntity(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<BrandResponseDTO>> getAllBrands() {
        var query = new GetAllBrandsQuery();
        List<Brand> brands = mediator.send(query);
        List<BrandResponseDTO> dtos =
                brands.stream().map(BrandResponseDTO::fromEntity).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandResponseDTO> updateBrand(
            @PathVariable Long id, @Valid @RequestBody UpdateBrandRequestDTO requestDTO) {
        var command = new UpdateBrandCommand(id, requestDTO.name(), requestDTO.description());
        Brand brand = mediator.send(command);
        return ResponseEntity.ok(BrandResponseDTO.fromEntity(brand));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        var command = new DeleteBrandCommand(id);
        mediator.send(command);
        return ResponseEntity.noContent().build();
    }
}

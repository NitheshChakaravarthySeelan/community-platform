package com.community.catalog.productread.interfaces.controller;

import com.community.catalog.productread.application.dto.ProductDTO;
import com.community.catalog.productread.application.handler.GetAllProductsQuery;
import com.community.catalog.productread.application.handler.GetProductByIdQuery;
import com.community.catalog.productread.application.mediator.Mediator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductReadController {

    private final Mediator mediator;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> result = mediator.send(new GetAllProductsQuery());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Optional<ProductDTO> result = mediator.send(new GetProductByIdQuery(id));
        return result.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

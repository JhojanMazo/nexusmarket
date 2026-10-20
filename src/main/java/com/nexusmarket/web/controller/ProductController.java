package com.nexusmarket.web.controller;

import com.nexusmarket.application.service.ProductService;
import com.nexusmarket.application.service.ProductService.ProductNotFoundException;
import com.nexusmarket.domain.model.Product;
import com.nexusmarket.web.dto.ProductRequest;
import com.nexusmarket.web.dto.ProductResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductRequest request) {
        try {
            Product product = productService.create(
                    request.name(),
                    request.variantAttribute(),
                    request.variantValue(),
                    request.price(),
                    request.currency(),
                    request.status(),
                    request.type()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ProductResponse.from(product));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", exception.getMessage()));
        }
    }

    @GetMapping
    public List<ProductResponse> findAll() {
        return productService.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(
                    ProductResponse.from(productService.findById(id)));
        } catch (ProductNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", exception.getMessage()));
        }
    }
}

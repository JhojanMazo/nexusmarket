package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Product;

public class ProductCatalogManagementService {

    public Product createProduct(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        return new Product(name, description);
    }
}
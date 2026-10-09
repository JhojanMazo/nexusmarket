package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Product;

public class ProductCatalogManagementService {

    public Product createProduct(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        return null; // TODO: initialize with correct parameters
    }
}
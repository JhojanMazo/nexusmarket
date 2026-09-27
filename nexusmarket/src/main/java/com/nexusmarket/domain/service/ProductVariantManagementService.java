package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Product;
import com.nexusmarket.domain.entity.Variant;

public class ProductVariantManagementService {

    public void addVariant(Product product, Variant variant) {
        if (product == null || variant == null) {
            throw new IllegalArgumentException("Product and Variant must not be null.");
        }
        product.getVariants().add(variant);
    }
}
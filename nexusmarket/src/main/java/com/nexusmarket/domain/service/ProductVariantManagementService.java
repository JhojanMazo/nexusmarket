package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Product;
import com.nexusmarket.domain.valueobject.ProductVariant;

public class ProductVariantManagementService {

    public void addVariant(Product product, com.nexusmarket.domain.valueobject.ProductVariant variant) {
        if (product == null || variant == null) {
            throw new IllegalArgumentException("Product and Variant must not be null.");
        }
        product.addVariant(variant);
    }
}
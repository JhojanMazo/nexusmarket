package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Product;

public class ProductTypeDifferentiationService {

    public void configureProductShipping(Product product) {
        if ("DIGITAL".equalsIgnoreCase(product.getType())) {
            product.setRequiresShipping(false);
        } else {
            product.setRequiresShipping(true);
        }
    }
}
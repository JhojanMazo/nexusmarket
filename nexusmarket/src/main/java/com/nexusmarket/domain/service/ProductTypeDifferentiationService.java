package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Product;

public class ProductTypeDifferentiationService {

    public void configureProductShipping(Product product) {
        if (product.getType() == com.nexusmarket.domain.enums.ProductType.DIGITAL) {
            // shipping logic here
        } else {
            // shipping logic here
        }
    }
}
package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Product;

public class ProductStateControlService {

    public void publishProduct(Product product) {
        product.setStatus(com.nexusmarket.domain.enums.ProductStatus.PUBLISHED);
    }

    public void suspendProduct(Product product) {
        product.setStatus(com.nexusmarket.domain.enums.ProductStatus.SUSPENDED);
    }

    public void discontinueProduct(Product product) {
        product.setStatus(com.nexusmarket.domain.enums.ProductStatus.DISCONTINUED);
    }
}
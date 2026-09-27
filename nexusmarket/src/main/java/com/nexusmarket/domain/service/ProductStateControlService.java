package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Product;

public class ProductStateControlService {

    public void publishProduct(Product product) {
        product.setState("PUBLISHED");
    }

    public void suspendProduct(Product product) {
        product.setState("SUSPENDED");
    }

    public void discontinueProduct(Product product) {
        product.setState("DISCONTINUED");
    }
}
package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.ShoppingCart;
import com.nexusmarket.domain.model.Product;

public class ShoppingCartManagementService {

    public void addItem(com.nexusmarket.domain.model.ShoppingCart cart, Product product, int quantity) {
        if (cart == null || product == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid shopping cart parameters.");
        }
        cart.addItem(product, quantity);
    }
}
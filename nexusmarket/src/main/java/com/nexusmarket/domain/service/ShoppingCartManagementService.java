package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Cart;
import com.nexusmarket.domain.entity.Product;

public class ShoppingCartManagementService {

    public void addItem(Cart cart, Product product, int quantity) {
        if (cart == null || product == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid shopping cart parameters.");
        }
        cart.addItem(product, quantity);
    }
}
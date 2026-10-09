package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Inventory;

public class InventoryAdjustmentsReturnsService {

    public void processReturn(Inventory inventory, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Return quantity must be positive.");
        }
        inventory.restockFromReturn(quantity);
    }

    public void adjustStock(Inventory inventory, int realQuantity) {
        if (realQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        inventory.setAvailableQuantity(realQuantity);
    }
}
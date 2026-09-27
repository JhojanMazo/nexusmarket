package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Inventory;

public class InventoryEntryRegistrationService {

    public void registerEntry(Inventory inventory, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity entered must be greater than zero.");
        }
        inventory.increaseStock(quantity);
    }
}
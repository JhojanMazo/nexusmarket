package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Inventory;

public class InventoryReservationService {

    public void reserve(Inventory inventory, int quantity) {
        if (inventory.getAvailableStock() < quantity) {
            throw new IllegalStateException("Insufficient inventory available to reserve.");
        }
        inventory.reserve(quantity);
    }
}
package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Inventory;

public class CriticalStockValidationService {

    public void validateStock(Inventory inventory, int requestedQuantity) {
        if (inventory.getAvailableQuantity() < requestedQuantity) {
            throw new IllegalStateException("Critical or insufficient stock available.");
        }
    }
}
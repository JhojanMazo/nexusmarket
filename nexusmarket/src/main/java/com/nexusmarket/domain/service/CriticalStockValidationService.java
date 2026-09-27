package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Inventory;

public class CriticalStockValidationService {

    public void validateStock(Inventory inventory, int requestedQuantity) {
        if (inventory.getAvailableStock() < requestedQuantity) {
            throw new IllegalStateException("Critical or insufficient stock available.");
        }
    }
}
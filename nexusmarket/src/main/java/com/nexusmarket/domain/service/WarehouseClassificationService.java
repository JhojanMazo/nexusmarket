package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Warehouse;

public class WarehouseClassificationService {

    public void classifyWarehouse(Warehouse warehouse, String type) {
        if (!"MARKETPLACE".equalsIgnoreCase(type) && !"SELLER".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("Invalid warehouse classification type.");
        }
        warehouse.setType(type.toUpperCase());
    }
}
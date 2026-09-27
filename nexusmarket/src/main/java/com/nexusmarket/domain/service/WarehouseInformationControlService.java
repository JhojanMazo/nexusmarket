package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Warehouse;

public class WarehouseInformationControlService {

    public void updateWarehouseCapacity(Warehouse warehouse, double capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative.");
        }
        warehouse.setCapacity(capacity);
    }
}
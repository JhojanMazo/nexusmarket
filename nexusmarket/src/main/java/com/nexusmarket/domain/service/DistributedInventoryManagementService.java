package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Inventory;
import com.nexusmarket.domain.entity.Product;
import com.nexusmarket.domain.entity.Warehouse;

public class DistributedInventoryManagementService {

    public Inventory linkInventory(Product product, Warehouse warehouse) {
        if (product == null || warehouse == null) {
            throw new IllegalArgumentException("Both Product and Warehouse are mandatory for linking inventory.");
        }
        return new Inventory(product, warehouse);
    }
}
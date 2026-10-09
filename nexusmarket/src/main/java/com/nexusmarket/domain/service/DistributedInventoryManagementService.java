package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Inventory;
import com.nexusmarket.domain.model.Product;
import com.nexusmarket.domain.model.Warehouse;

public class DistributedInventoryManagementService {

    public Inventory linkInventory(Product product, Warehouse warehouse) {
        if (product == null || warehouse == null) {
            throw new IllegalArgumentException("Both Product and Warehouse are mandatory for linking inventory.");
        }
        return new Inventory(product, warehouse);
    }
}
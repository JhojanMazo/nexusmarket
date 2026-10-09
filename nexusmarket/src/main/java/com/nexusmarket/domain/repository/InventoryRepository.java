package com.nexusmarket.domain.repository;

import com.nexusmarket.domain.model.Inventory;
public interface InventoryRepository {
    Inventory save(Inventory inv);
    Inventory findByWarehouseAndProduct(String wId, String pId);
}

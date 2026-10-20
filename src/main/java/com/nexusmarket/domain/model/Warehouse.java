package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.WarehouseType;
import com.nexusmarket.domain.enums.UserRole;
import com.nexusmarket.domain.valueobject.Address;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a Warehouse.
 *
 * FR‑007: If the warehouse type is SELLER, it must be associated with a Seller.
 */
public class Warehouse {
    private final UUID warehouseId;
    private final String name;
    private final Address address;
    private final WarehouseType type;
    private final UUID sellerId; // may be null if type == MARKETPLACE

    public Warehouse(UUID warehouseId, String name, Address address, WarehouseType type, UUID sellerId) {
        this.warehouseId = Objects.requireNonNull(warehouseId);
        this.name = Objects.requireNonNull(name);
        this.address = Objects.requireNonNull(address);
        this.type = Objects.requireNonNull(type);
        if (type == WarehouseType.SELLER) {
            if (sellerId == null) {
                throw new IllegalArgumentException("Seller warehouse must have a sellerId");
            }
        } else {
            // marketplace warehouses ignore sellerId
        }
        this.sellerId = sellerId;
    }

    public UUID getWarehouseId() { return warehouseId; }
    public String getName() { return name; }
    public Address getAddress() { return address; }
    public WarehouseType getType() { return type; }
    public UUID getSellerId() { return sellerId; }
}

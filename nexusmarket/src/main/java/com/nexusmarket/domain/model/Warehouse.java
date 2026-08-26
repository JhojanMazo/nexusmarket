package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.WarehouseType;
import com.nexusmarket.domain.valueobject.Address;

import java.util.Objects;
import java.util.Optional;

public class Warehouse {

    private final String warehouseId;
    private String name;
    private final WarehouseType type;
    private Address location;
    private final Seller associatedSeller; // present only when type == SELLER

    public Warehouse(String warehouseId, String name, WarehouseType type,
                      Address location, Seller associatedSeller) {
        this.warehouseId = requireNotBlank(warehouseId, "warehouseId");
        this.name = requireNotBlank(name, "name");
        this.type = Objects.requireNonNull(type, "type is required");
        this.location = Objects.requireNonNull(location, "location is required");
        if (type == WarehouseType.SELLER && associatedSeller == null) {
            throw new IllegalArgumentException("A SELLER-type warehouse requires an associated seller.");
        }
        this.associatedSeller = associatedSeller;
    }

    private static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be blank.");
        }
        return value;
    }

    public String getWarehouseId() { return warehouseId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = requireNotBlank(name, "name"); }
    public WarehouseType getType() { return type; }
    public Address getLocation() { return location; }
    public void setLocation(Address location) {
        this.location = Objects.requireNonNull(location, "location is required");
    }
    public Optional<Seller> getAssociatedSeller() { return Optional.ofNullable(associatedSeller); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Warehouse)) return false;
        return warehouseId.equals(((Warehouse) o).warehouseId);
    }

    @Override
    public int hashCode() { return Objects.hash(warehouseId); }
}

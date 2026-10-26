package application.domain.models;

import application.domain.enums.WarehouseType;
import application.domain.enums.UserRole;
import application.domain.valueobjects.Address;
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
    private double capacity;

    public Warehouse(UUID warehouseId, String name, Address address, WarehouseType type, UUID sellerId) {
        this(warehouseId, name, address, type, sellerId, 0.0);
    }

    public Warehouse(UUID warehouseId, String name, Address address, WarehouseType type, UUID sellerId, double capacity) {
        this.warehouseId = Objects.requireNonNull(warehouseId);
        this.name = Objects.requireNonNull(name);
        this.address = Objects.requireNonNull(address);
        this.type = Objects.requireNonNull(type);
        if (type == WarehouseType.SELLER) {
            if (sellerId == null) {
                throw new IllegalArgumentException("Seller warehouse must have a sellerId");
            }
        }
        this.sellerId = sellerId;
        updateCapacity(capacity);
    }

    public UUID getWarehouseId() { return warehouseId; }
    public String getName() { return name; }
    public Address getAddress() { return address; }
    public WarehouseType getType() { return type; }
    public UUID getSellerId() { return sellerId; }
    public double getCapacity() { return capacity; }

    public void updateCapacity(double capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacidad debe ser mayor o igual a cero");
        }
        this.capacity = capacity;
    }
}

package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.InventoryStatus;

import java.util.Objects;

/**
 * Stock, always tied to a specific Product and Warehouse.
 * Constraint: negative stock is never allowed, under any circumstance.
 * Validation: non-existent or damaged stock can never be reserved.
 */
public class Inventory {

    private final String inventoryId;
    private final Product product;
    private final Warehouse warehouse;
    private int availableQuantity;
    private int reservedQuantity;
    private InventoryStatus status;

    public Inventory(String inventoryId, Product product, Warehouse warehouse,
                      int availableQuantity, int reservedQuantity, InventoryStatus status) {
        this.inventoryId = requireNotBlank(inventoryId, "inventoryId");
        this.product = Objects.requireNonNull(product, "product is required");
        this.warehouse = Objects.requireNonNull(warehouse, "warehouse is required");
        this.status = Objects.requireNonNull(status, "status is required");
        requireNonNegative(availableQuantity, "availableQuantity");
        requireNonNegative(reservedQuantity, "reservedQuantity");
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
    }

    private static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be blank.");
        }
        return value;
    }

    private static void requireNonNegative(int quantity, String field) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be negative.");
        }
    }

    /** Reserves units of this inventory for an order. */
    public void reserve(int quantity) {
        if (status == InventoryStatus.DAMAGED) {
            throw new IllegalStateException("Inventory marked as Damaged cannot be reserved.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("The quantity to reserve must be greater than zero.");
        }
        if (quantity > availableQuantity) {
            throw new IllegalStateException("Not enough stock available to reserve.");
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }

    public void confirmSaleOutbound(int quantity) {
        if (quantity <= 0 || quantity > reservedQuantity) {
            throw new IllegalArgumentException("Invalid quantity to confirm sale outbound.");
        }
        reservedQuantity -= quantity;
    }

    /** Restocks units as a result of a return. */
    public void restockFromReturn(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("The quantity to restock must be greater than zero.");
        }
        availableQuantity += quantity;
    }

    public String getInventoryId() { return inventoryId; }
    public Product getProduct() { return product; }
    public Warehouse getWarehouse() { return warehouse; }
    public int getAvailableQuantity() { return availableQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public InventoryStatus getStatus() { return status; }
    public void setStatus(InventoryStatus status) {
        this.status = Objects.requireNonNull(status, "status is required");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Inventory)) return false;
        return inventoryId.equals(((Inventory) o).inventoryId);
    }

    @Override
    public int hashCode() { return Objects.hash(inventoryId); }
}

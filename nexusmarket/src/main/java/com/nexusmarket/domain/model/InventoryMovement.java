package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.InventoryMovementType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable historical record of every change to an Inventory, guaranteeing
 * traceability.
 */
public final class InventoryMovement {

    private final String movementId;
    private final Inventory inventory;
    private final InventoryMovementType type;
    private final int quantity;
    private final LocalDateTime date;
    private final String reference; // optional: originating document/process (e.g. orderId)

    public InventoryMovement(String movementId, Inventory inventory, InventoryMovementType type,
                              int quantity, LocalDateTime date, String reference) {
        this.movementId = requireNotBlank(movementId, "movementId");
        this.inventory = Objects.requireNonNull(inventory, "inventory is required");
        this.type = Objects.requireNonNull(type, "type is required");
        if (quantity == 0) {
            throw new IllegalArgumentException("The movement quantity cannot be zero.");
        }
        this.quantity = quantity;
        this.date = Objects.requireNonNull(date, "date is required");
        this.reference = reference;
    }

    private static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be blank.");
        }
        return value;
    }

    public String getMovementId() { return movementId; }
    public Inventory getInventory() { return inventory; }
    public InventoryMovementType getType() { return type; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getDate() { return date; }
    public Optional<String> getReference() { return Optional.ofNullable(reference); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryMovement)) return false;
        return movementId.equals(((InventoryMovement) o).movementId);
    }

    @Override
    public int hashCode() { return Objects.hash(movementId); }
}

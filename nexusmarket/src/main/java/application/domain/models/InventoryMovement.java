package application.domain.models;

import application.domain.enums.InventoryMovementType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable audit record of an inventory quantity change (FR-015).
 */
public record InventoryMovement(
        UUID movementId,
        UUID inventoryId,
        InventoryMovementType type,
        int quantity,
        Instant date,
        String reference
) {
    public InventoryMovement {
        Objects.requireNonNull(movementId);
        Objects.requireNonNull(inventoryId);
        Objects.requireNonNull(type);
        Objects.requireNonNull(date);
        if (quantity == 0) {
            throw new IllegalArgumentException("El movimiento debe tener una cantidad distinta de cero");
        }
    }
}

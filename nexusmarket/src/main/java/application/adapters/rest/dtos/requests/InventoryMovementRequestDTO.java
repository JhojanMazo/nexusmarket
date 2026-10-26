package application.adapters.rest.dtos.requests;

import application.domain.enums.InventoryMovementType;

import java.util.UUID;

public record InventoryMovementRequestDTO(
        UUID inventoryId,
        InventoryMovementType type,
        int quantity,
        String reference
) {
}

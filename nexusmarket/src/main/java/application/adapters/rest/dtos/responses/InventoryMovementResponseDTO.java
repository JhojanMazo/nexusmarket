package application.adapters.rest.dtos.responses;

import application.domain.enums.InventoryMovementType;

import java.time.Instant;
import java.util.UUID;

public record InventoryMovementResponseDTO(
        UUID movementId,
        UUID inventoryId,
        InventoryMovementType type,
        int quantity,
        Instant date,
        String reference
) {
}

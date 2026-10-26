package application.adapters.rest.dtos.responses;

import application.domain.enums.InventoryStatus;

import java.util.UUID;

public record InventoryResponseDTO(
        UUID inventoryId,
        UUID productId,
        String productName,
        UUID warehouseId,
        String warehouseName,
        int availableQuantity,
        int reservedQuantity,
        InventoryStatus status
) {
}

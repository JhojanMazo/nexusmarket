package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.InventoryMovementResponseDTO;
import application.adapters.rest.dtos.responses.InventoryResponseDTO;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class InventoryRestMapper {

    public InventoryResponseDTO toResponse(Inventory inventory) {
        if (inventory == null) return null;
        return new InventoryResponseDTO(
                inventory.getInventoryId(),
                inventory.getProduct() != null ? inventory.getProduct().getProductId() : null,
                inventory.getProduct() != null ? inventory.getProduct().getName() : null,
                inventory.getWarehouse() != null ? inventory.getWarehouse().getWarehouseId() : null,
                inventory.getWarehouse() != null ? inventory.getWarehouse().getName() : null,
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                inventory.getStatus()
        );
    }

    public List<InventoryResponseDTO> toResponseList(List<Inventory> inventories) {
        if (inventories == null) return Collections.emptyList();
        return inventories.stream().map(this::toResponse).toList();
    }

    public InventoryMovementResponseDTO toMovementResponse(InventoryMovement movement) {
        if (movement == null) return null;
        return new InventoryMovementResponseDTO(
                movement.movementId(),
                movement.inventoryId(),
                movement.type(),
                movement.quantity(),
                movement.date(),
                movement.reference()
        );
    }

    public List<InventoryMovementResponseDTO> toMovementResponseList(List<InventoryMovement> movements) {
        if (movements == null) return Collections.emptyList();
        return movements.stream().map(this::toMovementResponse).toList();
    }
}

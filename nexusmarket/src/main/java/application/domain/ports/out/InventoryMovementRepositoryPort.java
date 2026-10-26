package application.domain.ports.out;

import application.domain.models.InventoryMovement;

import java.util.List;
import java.util.UUID;

public interface InventoryMovementRepositoryPort {
    InventoryMovement save(InventoryMovement movement);
    List<InventoryMovement> findByInventoryId(UUID inventoryId);
    List<InventoryMovement> findAll();
}

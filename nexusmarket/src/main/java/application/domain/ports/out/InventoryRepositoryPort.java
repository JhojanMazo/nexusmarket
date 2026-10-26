package application.domain.ports.out;

import application.domain.models.Inventory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepositoryPort {
    Inventory save(Inventory inventory);
    Optional<Inventory> findById(UUID inventoryId);
    Optional<Inventory> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId);
    List<Inventory> findByWarehouseId(UUID warehouseId);
    List<Inventory> findByProductId(UUID productId);
    List<Inventory> findAll();
}

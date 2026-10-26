package application.domain.services.inventory;

import application.domain.models.Inventory;
import application.domain.ports.out.InventoryRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryInventoryRepository implements InventoryRepositoryPort {

    private final Map<UUID, Inventory> storage = new ConcurrentHashMap<>();

    @Override
    public Inventory save(Inventory inventory) {
        storage.put(inventory.getInventoryId(), inventory);
        return inventory;
    }

    @Override
    public Optional<Inventory> findById(UUID inventoryId) {
        return Optional.ofNullable(storage.get(inventoryId));
    }

    @Override
    public Optional<Inventory> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId) {
        return storage.values().stream()
                .filter(i -> i.getProduct().getProductId().equals(productId) &&
                        i.getWarehouse().getWarehouseId().equals(warehouseId))
                .findFirst();
    }

    @Override
    public List<Inventory> findByWarehouseId(UUID warehouseId) {
        return storage.values().stream()
                .filter(i -> i.getWarehouse().getWarehouseId().equals(warehouseId))
                .toList();
    }

    @Override
    public List<Inventory> findByProductId(UUID productId) {
        return storage.values().stream()
                .filter(i -> i.getProduct().getProductId().equals(productId))
                .toList();
    }

    @Override
    public List<Inventory> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void clear() {
        storage.clear();
    }
}

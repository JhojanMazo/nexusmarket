package application.domain.services.inventory;

import application.domain.enums.WarehouseType;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryWarehouseRepository implements WarehouseRepositoryPort {

    private final Map<UUID, Warehouse> storage = new ConcurrentHashMap<>();

    @Override
    public Warehouse save(Warehouse warehouse) {
        storage.put(warehouse.getWarehouseId(), warehouse);
        return warehouse;
    }

    @Override
    public Optional<Warehouse> findById(UUID warehouseId) {
        return Optional.ofNullable(storage.get(warehouseId));
    }

    @Override
    public List<Warehouse> findBySellerId(UUID sellerId) {
        return storage.values().stream()
                .filter(w -> sellerId.equals(w.getSellerId()))
                .toList();
    }

    @Override
    public List<Warehouse> findByType(WarehouseType type) {
        return storage.values().stream()
                .filter(w -> w.getType() == type)
                .toList();
    }

    @Override
    public List<Warehouse> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void clear() {
        storage.clear();
    }
}

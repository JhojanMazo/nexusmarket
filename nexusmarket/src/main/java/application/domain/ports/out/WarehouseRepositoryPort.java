package application.domain.ports.out;

import application.domain.enums.WarehouseType;
import application.domain.models.Warehouse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepositoryPort {
    Warehouse save(Warehouse warehouse);
    Optional<Warehouse> findById(UUID warehouseId);
    List<Warehouse> findBySellerId(UUID sellerId);
    List<Warehouse> findByType(WarehouseType type);
    List<Warehouse> findAll();
}

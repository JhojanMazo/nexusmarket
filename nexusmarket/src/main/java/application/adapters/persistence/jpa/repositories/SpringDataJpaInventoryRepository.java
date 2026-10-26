package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.InventoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataJpaInventoryRepository extends JpaRepository<InventoryJpaEntity, UUID> {
    Optional<InventoryJpaEntity> findByProductProductIdAndWarehouseWarehouseId(UUID productId, UUID warehouseId);
    List<InventoryJpaEntity> findByWarehouseWarehouseId(UUID warehouseId);
    List<InventoryJpaEntity> findByProductProductId(UUID productId);
}

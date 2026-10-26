package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.InventoryMovementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataJpaInventoryMovementRepository extends JpaRepository<InventoryMovementJpaEntity, UUID> {
    List<InventoryMovementJpaEntity> findByInventoryInventoryId(UUID inventoryId);
}

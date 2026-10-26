package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import application.domain.enums.WarehouseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataJpaWarehouseRepository extends JpaRepository<WarehouseJpaEntity, UUID> {
    List<WarehouseJpaEntity> findBySellerId(UUID sellerId);
    List<WarehouseJpaEntity> findByType(WarehouseType type);
}

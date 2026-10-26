package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.InventoryJpaEntity;
import application.adapters.persistence.jpa.entities.InventoryMovementJpaEntity;
import application.adapters.persistence.jpa.mappers.InventoryMovementJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInventoryMovementRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInventoryRepository;
import application.domain.models.InventoryMovement;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Primary
@Repository
public class InventoryMovementJpaAdapter implements InventoryMovementRepositoryPort {

    private final SpringDataJpaInventoryMovementRepository jpaRepository;
    private final SpringDataJpaInventoryRepository inventoryRepository;
    private final InventoryMovementJpaMapper mapper;

    public InventoryMovementJpaAdapter(SpringDataJpaInventoryMovementRepository jpaRepository,
                                      SpringDataJpaInventoryRepository inventoryRepository,
                                      InventoryMovementJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.inventoryRepository = inventoryRepository;
        this.mapper = mapper;
    }

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        InventoryJpaEntity inventoryEntity = movement.inventoryId() != null
                ? inventoryRepository.findById(movement.inventoryId()).orElse(null)
                : null;
        InventoryMovementJpaEntity entity = mapper.toEntity(movement, inventoryEntity);
        InventoryMovementJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<InventoryMovement> findByInventoryId(UUID inventoryId) {
        return jpaRepository.findByInventoryInventoryId(inventoryId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<InventoryMovement> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}

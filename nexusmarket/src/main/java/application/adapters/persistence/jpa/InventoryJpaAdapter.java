package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.InventoryJpaEntity;
import application.adapters.persistence.jpa.mappers.InventoryJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInventoryRepository;
import application.domain.models.Inventory;
import application.domain.ports.out.InventoryRepositoryPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public class InventoryJpaAdapter implements InventoryRepositoryPort {

    private final SpringDataJpaInventoryRepository jpaRepository;
    private final InventoryJpaMapper mapper;

    public InventoryJpaAdapter(SpringDataJpaInventoryRepository jpaRepository, InventoryJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Inventory save(Inventory inventory) {
        InventoryJpaEntity entity = mapper.toEntity(inventory);
        InventoryJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Inventory> findById(UUID inventoryId) {
        return jpaRepository.findById(inventoryId).map(mapper::toDomain);
    }

    @Override
    public Optional<Inventory> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId) {
        return jpaRepository.findByProductProductIdAndWarehouseWarehouseId(productId, warehouseId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Inventory> findByWarehouseId(UUID warehouseId) {
        return jpaRepository.findByWarehouseWarehouseId(warehouseId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Inventory> findByProductId(UUID productId) {
        return jpaRepository.findByProductProductId(productId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Inventory> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}

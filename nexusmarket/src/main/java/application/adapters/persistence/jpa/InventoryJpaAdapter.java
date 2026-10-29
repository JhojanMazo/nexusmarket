package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.InventoryJpaEntity;
import application.adapters.persistence.jpa.mappers.InventoryJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInventoryRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaWarehouseRepository;
import application.domain.models.Inventory;
import application.domain.ports.out.InventoryRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public class InventoryJpaAdapter implements InventoryRepositoryPort {

    private final SpringDataJpaInventoryRepository jpaRepository;
    private final InventoryJpaMapper mapper;
    private final SpringDataJpaProductRepository productJpaRepository;
    private final SpringDataJpaWarehouseRepository warehouseJpaRepository;

    @Autowired
    public InventoryJpaAdapter(SpringDataJpaInventoryRepository jpaRepository,
                               InventoryJpaMapper mapper,
                               SpringDataJpaProductRepository productJpaRepository,
                               SpringDataJpaWarehouseRepository warehouseJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.productJpaRepository = productJpaRepository;
        this.warehouseJpaRepository = warehouseJpaRepository;
    }

    public InventoryJpaAdapter(SpringDataJpaInventoryRepository jpaRepository, InventoryJpaMapper mapper) {
        this(jpaRepository, mapper, null, null);
    }

    @Override
    @Transactional
    public Inventory save(Inventory inventory) {
        InventoryJpaEntity entity = jpaRepository.findById(inventory.getInventoryId())
                .map(existing -> mapper.updateEntity(existing, inventory))
                .orElseGet(() -> {
                    InventoryJpaEntity created = mapper.toEntity(inventory);
                    if (productJpaRepository != null && inventory.getProduct() != null) {
                        productJpaRepository.findById(inventory.getProduct().getProductId())
                                .ifPresent(created::setProduct);
                    }
                    if (warehouseJpaRepository != null && inventory.getWarehouse() != null) {
                        warehouseJpaRepository.findById(inventory.getWarehouse().getWarehouseId())
                                .ifPresent(created::setWarehouse);
                    }
                    return created;
                });
        InventoryJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Inventory> findById(UUID inventoryId) {
        return jpaRepository.findById(inventoryId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Inventory> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId) {
        return jpaRepository.findByProductProductIdAndWarehouseWarehouseId(productId, warehouseId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findByWarehouseId(UUID warehouseId) {
        return jpaRepository.findByWarehouseWarehouseId(warehouseId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findByProductId(UUID productId) {
        return jpaRepository.findByProductProductId(productId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}

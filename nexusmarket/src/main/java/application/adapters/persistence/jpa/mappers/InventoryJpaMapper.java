package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.InventoryJpaEntity;
import application.adapters.persistence.jpa.entities.InventoryMovementJpaEntity;
import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Product;
import application.domain.models.Warehouse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class InventoryJpaMapper {

    private final ProductJpaMapper productJpaMapper;
    private final WarehouseJpaMapper warehouseJpaMapper;
    private final InventoryMovementJpaMapper inventoryMovementJpaMapper;

    public InventoryJpaMapper(ProductJpaMapper productJpaMapper,
                              WarehouseJpaMapper warehouseJpaMapper,
                              InventoryMovementJpaMapper inventoryMovementJpaMapper) {
        this.productJpaMapper = productJpaMapper;
        this.warehouseJpaMapper = warehouseJpaMapper;
        this.inventoryMovementJpaMapper = inventoryMovementJpaMapper;
    }

    public Inventory toDomain(InventoryJpaEntity entity) {
        if (entity == null) return null;

        Product product = productJpaMapper.toDomain(entity.getProduct());
        Warehouse warehouse = warehouseJpaMapper.toDomain(entity.getWarehouse());
        List<InventoryMovement> movements = entity.getMovements() != null
                ? entity.getMovements().stream().map(inventoryMovementJpaMapper::toDomain).toList()
                : Collections.emptyList();

        return Inventory.reconstitute(
                entity.getInventoryId(),
                product,
                warehouse,
                entity.getAvailableQuantity(),
                entity.getReservedQuantity(),
                entity.getStatus(),
                movements
        );
    }

    public InventoryJpaEntity toEntity(Inventory domain) {
        if (domain == null) return null;

        ProductJpaEntity productEntity = productJpaMapper.toEntity(domain.getProduct());
        WarehouseJpaEntity warehouseEntity = warehouseJpaMapper.toEntity(domain.getWarehouse());

        InventoryJpaEntity entity = new InventoryJpaEntity(
                domain.getInventoryId(),
                productEntity,
                warehouseEntity,
                domain.getAvailableQuantity(),
                domain.getReservedQuantity(),
                domain.getStatus()
        );

        if (domain.getMovements() != null) {
            List<InventoryMovementJpaEntity> movementEntities = domain.getMovements().stream()
                    .map(m -> inventoryMovementJpaMapper.toEntity(m, entity))
                    .toList();
            entity.getMovements().addAll(movementEntities);
        }

        return entity;
    }

    /**
     * Updates an existing (managed) {@link InventoryJpaEntity} in place so its optimistic
     * lock {@code version}, audit timestamps ({@code createdAt}, {@code updatedAt}) and
     * managed associations are preserved.
     */
    public InventoryJpaEntity updateEntity(InventoryJpaEntity existing, Inventory domain) {
        if (existing == null || domain == null) return existing;

        existing.setAvailableQuantity(domain.getAvailableQuantity());
        existing.setReservedQuantity(domain.getReservedQuantity());
        existing.setStatus(domain.getStatus());

        if (domain.getMovements() != null) {
            Set<UUID> existingIds = existing.getMovements().stream()
                    .map(InventoryMovementJpaEntity::getMovementId)
                    .collect(Collectors.toSet());
            for (InventoryMovement movement : domain.getMovements()) {
                if (!existingIds.contains(movement.movementId())) {
                    existing.getMovements().add(inventoryMovementJpaMapper.toEntity(movement, existing));
                }
            }
        }

        return existing;
    }
}

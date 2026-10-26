package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.InventoryJpaEntity;
import application.adapters.persistence.jpa.entities.InventoryMovementJpaEntity;
import application.domain.models.InventoryMovement;
import org.springframework.stereotype.Component;

@Component
public class InventoryMovementJpaMapper {

    public InventoryMovement toDomain(InventoryMovementJpaEntity entity) {
        if (entity == null) return null;
        return new InventoryMovement(
                entity.getMovementId(),
                entity.getInventory() != null ? entity.getInventory().getInventoryId() : null,
                entity.getType(),
                entity.getQuantity(),
                entity.getDate(),
                entity.getReference()
        );
    }

    public InventoryMovementJpaEntity toEntity(InventoryMovement domain, InventoryJpaEntity inventoryEntity) {
        if (domain == null) return null;
        return new InventoryMovementJpaEntity(
                domain.movementId(),
                inventoryEntity,
                domain.type(),
                domain.quantity(),
                domain.date(),
                domain.reference()
        );
    }
}

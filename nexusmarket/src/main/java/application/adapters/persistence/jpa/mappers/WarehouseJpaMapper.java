package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.AddressEmbeddable;
import application.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import application.domain.models.Warehouse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseJpaMapper {

    public Warehouse toDomain(WarehouseJpaEntity entity) {
        if (entity == null) return null;
        return new Warehouse(
                entity.getWarehouseId(),
                entity.getName(),
                entity.getAddress() != null ? entity.getAddress().toDomain() : null,
                entity.getType(),
                entity.getSellerId(),
                entity.getCapacity()
        );
    }

    public WarehouseJpaEntity toEntity(Warehouse domain) {
        if (domain == null) return null;
        return new WarehouseJpaEntity(
                domain.getWarehouseId(),
                domain.getName(),
                AddressEmbeddable.fromDomain(domain.getAddress()),
                domain.getType(),
                domain.getSellerId(),
                domain.getCapacity()
        );
    }
}

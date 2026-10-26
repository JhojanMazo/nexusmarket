package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.OperationJpaEntity;
import application.domain.models.Operation;

public final class OperationJpaMapper {

    private OperationJpaMapper() {
    }

    public static OperationJpaEntity toEntity(Operation domain) {
        if (domain == null) {
            return null;
        }
        return new OperationJpaEntity(
                domain.getId(),
                domain.getOperationType(),
                domain.getDescription(),
                domain.getOperatorId(),
                domain.getStatus(),
                domain.getPayload(),
                domain.getCreatedAt()
        );
    }

    public static Operation toDomain(OperationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Operation(
                entity.getId(),
                entity.getOperationType(),
                entity.getDescription(),
                entity.getOperatorId(),
                entity.getStatus(),
                entity.getPayload(),
                entity.getCreatedAt()
        );
    }
}

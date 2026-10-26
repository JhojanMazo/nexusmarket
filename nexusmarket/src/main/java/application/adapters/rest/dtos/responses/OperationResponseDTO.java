package application.adapters.rest.dtos.responses;

import java.time.Instant;
import java.util.UUID;

public record OperationResponseDTO(
        UUID id,
        String operationType,
        String description,
        UUID operatorId,
        String status,
        String payload,
        Instant createdAt
) {
}

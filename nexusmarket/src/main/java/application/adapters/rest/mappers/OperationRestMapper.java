package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.OperationResponseDTO;
import application.domain.models.Operation;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class OperationRestMapper {

    public OperationRestMapper() {
    }

    public static OperationResponseDTO toResponse(Operation domain) {
        if (domain == null) {
            return null;
        }
        return new OperationResponseDTO(
                domain.getId(),
                domain.getOperationType(),
                domain.getDescription(),
                domain.getOperatorId(),
                domain.getStatus(),
                domain.getPayload(),
                domain.getCreatedAt()
        );
    }

    public static List<OperationResponseDTO> toResponseList(List<Operation> operations) {
        if (operations == null) {
            return Collections.emptyList();
        }
        return operations.stream().map(OperationRestMapper::toResponse).toList();
    }
}

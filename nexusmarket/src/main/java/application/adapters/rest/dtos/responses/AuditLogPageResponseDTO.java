package application.adapters.rest.dtos.responses;

import java.util.List;

public record AuditLogPageResponseDTO(
        List<AuditLogResponseDTO> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isLast
) {
}

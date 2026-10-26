package application.adapters.rest.dtos.responses;

import application.domain.enums.AuditSeverity;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponseDTO(
        UUID id,
        String action,
        UUID userId,
        String userEmail,
        String resource,
        String resourceId,
        AuditSeverity severity,
        String details,
        String ipAddress,
        Instant timestamp
) {
}

package application.domain.ports.out;

import application.domain.models.AuditLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for AuditLog documents (typically stored in MongoDB).
 */
public interface AuditLogRepositoryPort {
    AuditLog save(AuditLog auditLog);
    Optional<AuditLog> findById(UUID id);
    List<AuditLog> findByUserId(UUID userId);
    List<AuditLog> findAll();
}

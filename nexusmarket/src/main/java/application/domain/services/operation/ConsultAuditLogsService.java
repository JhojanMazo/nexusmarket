package application.domain.services.operation;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.AuditLog;
import application.domain.ports.out.AuditLogRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Domain service to query audit logs.
 */
@Service
public class ConsultAuditLogsService {

    private final AuditLogRepositoryPort auditLogRepositoryPort;

    public ConsultAuditLogsService(AuditLogRepositoryPort auditLogRepositoryPort) {
        this.auditLogRepositoryPort = auditLogRepositoryPort;
    }

    public AuditLog findById(UUID auditLogId) {
        return auditLogRepositoryPort.findById(auditLogId)
                .orElseThrow(() -> new EntityNotFoundException("AuditLog", auditLogId));
    }

    public List<AuditLog> findByUserId(UUID userId) {
        return auditLogRepositoryPort.findByUserId(userId);
    }

    public List<AuditLog> findAll() {
        return auditLogRepositoryPort.findAll();
    }
}

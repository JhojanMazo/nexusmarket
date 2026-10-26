package application.domain.services.operation;

import application.domain.enums.AuditSeverity;
import application.domain.models.AuditLog;
import application.domain.ports.out.AuditLogRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Domain service to record enterprise audit logs into MongoDB.
 */
@Service
public class RegisterAuditLogService {

    private final AuditLogRepositoryPort auditLogRepositoryPort;

    public RegisterAuditLogService(AuditLogRepositoryPort auditLogRepositoryPort) {
        this.auditLogRepositoryPort = auditLogRepositoryPort;
    }

    public AuditLog execute(String action, UUID userId, String userEmail, String resource,
                            String resourceId, AuditSeverity severity, String details, String ipAddress) {
        AuditLog auditLog = AuditLog.create(action, userId, userEmail, resource, resourceId, severity, details, ipAddress);
        return auditLogRepositoryPort.save(auditLog);
    }
}

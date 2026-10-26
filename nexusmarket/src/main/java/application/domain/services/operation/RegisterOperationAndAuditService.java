package application.domain.services.operation;

import application.domain.enums.AuditSeverity;
import application.domain.models.AuditLog;
import application.domain.models.Operation;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Domain service coordinating relational operation persistence and MongoDB audit logging.
 */
@Service
public class RegisterOperationAndAuditService {

    private final RegisterOperationService registerOperationService;
    private final RegisterAuditLogService registerAuditLogService;

    public RegisterOperationAndAuditService(RegisterOperationService registerOperationService,
                                            RegisterAuditLogService registerAuditLogService) {
        this.registerOperationService = registerOperationService;
        this.registerAuditLogService = registerAuditLogService;
    }

    public Operation execute(String operationType, String description, UUID operatorId, String userEmail,
                             String resource, String resourceId, String payload, String ipAddress) {
        Operation operation = registerOperationService.execute(operationType, description, operatorId, payload);
        registerAuditLogService.execute(operationType, operatorId, userEmail, resource, resourceId,
                AuditSeverity.MEDIUM, description, ipAddress);
        return operation;
    }
}

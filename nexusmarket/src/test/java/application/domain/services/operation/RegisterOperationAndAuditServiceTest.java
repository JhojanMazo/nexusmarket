package application.domain.services.operation;

import application.domain.enums.AuditSeverity;
import application.domain.models.AuditLog;
import application.domain.models.Operation;
import application.domain.services.operation.RegisterAuditLogService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.services.operation.RegisterOperationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterOperationAndAuditServiceTest {

    @Mock
    private RegisterOperationService registerOperationService;

    @Mock
    private RegisterAuditLogService registerAuditLogService;

    private RegisterOperationAndAuditService service;

    @BeforeEach
    void setUp() {
        service = new RegisterOperationAndAuditService(registerOperationService, registerAuditLogService);
    }

    @Test
    void shouldCoordinateRelationalOperationAndMongoAudit() {
        UUID operatorId = UUID.randomUUID();
        Operation op = new Operation(UUID.randomUUID(), "ORDER_CANCELLED", "User cancelled order", operatorId, "COMPLETED", "{}", Instant.now());
        AuditLog audit = new AuditLog(UUID.randomUUID(), "ORDER_CANCELLED", operatorId, "user@test.com", "Order", "order-123", AuditSeverity.MEDIUM, "User cancelled order", "192.168.1.1", Instant.now());

        when(registerOperationService.execute("ORDER_CANCELLED", "User cancelled order", operatorId, "{}"))
                .thenReturn(op);
        when(registerAuditLogService.execute(eq("ORDER_CANCELLED"), eq(operatorId), eq("user@test.com"), eq("Order"), eq("order-123"), eq(AuditSeverity.MEDIUM), eq("User cancelled order"), eq("192.168.1.1")))
                .thenReturn(audit);

        Operation result = service.execute("ORDER_CANCELLED", "User cancelled order", operatorId, "user@test.com", "Order", "order-123", "{}", "192.168.1.1");

        assertNotNull(result);
        assertEquals(op.getId(), result.getId());
        verify(registerOperationService).execute("ORDER_CANCELLED", "User cancelled order", operatorId, "{}");
        verify(registerAuditLogService).execute(eq("ORDER_CANCELLED"), eq(operatorId), eq("user@test.com"), eq("Order"), eq("order-123"), eq(AuditSeverity.MEDIUM), eq("User cancelled order"), eq("192.168.1.1"));
    }
}

package application.adapters.aspect;

import application.adapters.security.SecurityUserContext;
import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import application.domain.enums.AuditSeverity;
import application.domain.models.AuthenticationResult;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Invoice;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.models.ShoppingCart;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.services.operation.RegisterAuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Cross-cutting Spring AOP aspect that automatically records critical business operations
 * into MongoDB through {@link RegisterAuditLogService} without coupling use cases or controllers
 * to manual audit invocations.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    private static final String DEFAULT_IP = "127.0.0.1";
    private static final String ANONYMOUS_EMAIL = "anonymous@system";

    private final RegisterAuditLogService registerAuditLogService;

    public AuditAspect(RegisterAuditLogService registerAuditLogService) {
        this.registerAuditLogService = registerAuditLogService;
    }

    /**
     * Matches any method explicitly annotated with {@link Auditable}.
     */
    @Pointcut("@annotation(application.adapters.aspect.Auditable) || @within(application.adapters.aspect.Auditable)")
    public void auditableAnnotated() {
    }

    /**
     * Matches state-changing operations across use case adapters (excluding read-only queries).
     */
    @Pointcut("execution(public * application.adapters.useCases.*UseCaseImpl.*(..)) " +
            "&& !execution(public * application.adapters.useCases.*UseCaseImpl.get*(..)) " +
            "&& !execution(public * application.adapters.useCases.*UseCaseImpl.list*(..)) " +
            "&& !execution(public * application.adapters.useCases.*UseCaseImpl.find*(..))")
    public void criticalUseCaseExecution() {
    }

    @Around("auditableAnnotated() || criticalUseCaseExecution()")
    public Object auditCriticalOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        Auditable auditable = resolveAuditableAnnotation(joinPoint);
        String action = resolveAction(joinPoint, auditable);
        String resource = resolveResource(joinPoint, auditable);
        AuditSeverity baseSeverity = auditable != null ? auditable.severity() : inferSeverity(joinPoint.getSignature().getName());

        try {
            Object result = joinPoint.proceed();
            recordAuditSafely(joinPoint, result, null, action, resource, baseSeverity);
            return result;
        } catch (Throwable ex) {
            AuditSeverity failureSeverity = baseSeverity == AuditSeverity.CRITICAL
                    ? AuditSeverity.CRITICAL
                    : AuditSeverity.HIGH;
            recordAuditSafely(joinPoint, null, ex, action, resource, failureSeverity);
            throw ex;
        }
    }

    private void recordAuditSafely(ProceedingJoinPoint joinPoint,
                                   Object result,
                                   Throwable error,
                                   String action,
                                   String resource,
                                   AuditSeverity severity) {
        try {
            Optional<AuthenticatedUserPrincipal> principalOpt = SecurityUserContext.get();
            UUID userId = principalOpt.map(AuthenticatedUserPrincipal::userId)
                    .orElseGet(() -> extractUserIdFallback(result, joinPoint.getArgs()));
            String userEmail = principalOpt.map(AuthenticatedUserPrincipal::email)
                    .orElseGet(() -> extractUserEmailFallback(result, joinPoint.getArgs()));
            String resourceId = extractResourceId(result, joinPoint.getArgs());
            String ipAddress = resolveClientIp();
            String details = buildDetails(joinPoint, error);

            registerAuditLogService.execute(
                    action,
                    userId,
                    userEmail,
                    resource,
                    resourceId,
                    severity,
                    details,
                    ipAddress
            );
        } catch (Exception auditEx) {
            log.warn("Audit logging failed for action {}: {}", action, auditEx.getMessage());
        }
    }

    private Auditable resolveAuditableAnnotation(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            Method method = methodSignature.getMethod();
            if (joinPoint.getTarget() != null) {
                try {
                    Method targetMethod = joinPoint.getTarget().getClass()
                            .getMethod(method.getName(), method.getParameterTypes());
                    Auditable found = AnnotationUtils.findAnnotation(targetMethod, Auditable.class);
                    if (found != null) {
                        return found;
                    }
                } catch (NoSuchMethodException ignored) {
                    // Fall back to interface/signature method
                }
            }
            Auditable onMethod = AnnotationUtils.findAnnotation(method, Auditable.class);
            if (onMethod != null) {
                return onMethod;
            }
        }
        return joinPoint.getTarget() != null
                ? AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), Auditable.class)
                : null;
    }

    private String resolveAction(ProceedingJoinPoint joinPoint, Auditable auditable) {
        if (auditable != null && auditable.action() != null && !auditable.action().isBlank()) {
            return auditable.action();
        }
        String methodName = joinPoint.getSignature().getName();
        return camelToUpperSnake(methodName);
    }

    private String resolveResource(ProceedingJoinPoint joinPoint, Auditable auditable) {
        if (auditable != null && auditable.resource() != null && !auditable.resource().isBlank()) {
            return auditable.resource();
        }
        if (joinPoint.getSignature() instanceof MethodSignature ms) {
            Class<?> returnType = ms.getReturnType();
            if (returnType != null && returnType != Void.TYPE && returnType != Object.class) {
                return returnType.getSimpleName();
            }
        }
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        return className.replace("UseCaseImpl", "").replace("RestController", "");
    }

    private AuditSeverity inferSeverity(String methodName) {
        String lower = methodName.toLowerCase(Locale.ROOT);
        if (lower.contains("payment") || lower.contains("refund") || lower.contains("invoice")
                || lower.contains("status") || lower.contains("approve") || lower.contains("reject")) {
            return AuditSeverity.HIGH;
        }
        if (lower.contains("login") || lower.contains("authenticate") || lower.contains("logout")) {
            return AuditSeverity.LOW;
        }
        return AuditSeverity.MEDIUM;
    }

    private String extractResourceId(Object result, Object[] args) {
        if (result != null) {
            if (result instanceof Order o && o.getOrderId() != null) return o.getOrderId().toString();
            if (result instanceof Product p && p.getProductId() != null) return p.getProductId().toString();
            if (result instanceof Inventory i && i.getInventoryId() != null) return i.getInventoryId().toString();
            if (result instanceof InventoryMovement m && m.movementId() != null) return m.movementId().toString();
            if (result instanceof Invoice inv && inv.getInvoiceId() != null) return inv.getInvoiceId().toString();
            if (result instanceof Shipment s && s.getShipmentId() != null) return s.getShipmentId().toString();
            if (result instanceof ReturnRequest r && r.getRequestId() != null) return r.getRequestId().toString();
            if (result instanceof Refund ref && ref.getRefundId() != null) return ref.getRefundId().toString();
            if (result instanceof Warehouse w && w.getWarehouseId() != null) return w.getWarehouseId().toString();
            if (result instanceof ShoppingCart c && c.getCartId() != null) return c.getCartId().toString();
            if (result instanceof User u && u.getUserId() != null) return u.getUserId().toString();
            if (result instanceof AuthenticationResult auth && auth.userId() != null) return auth.userId().toString();
            if (result instanceof Operation op && op.getId() != null) return op.getId().toString();
        }
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof UUID uuid) {
                    return uuid.toString();
                }
            }
        }
        return "N/A";
    }

    private UUID extractUserIdFallback(Object result, Object[] args) {
        if (result instanceof AuthenticationResult auth && auth.userId() != null) {
            return auth.userId();
        }
        if (result instanceof User user && user.getUserId() != null) {
            return user.getUserId();
        }
        if (result instanceof Order order && order.getBuyer() != null) {
            return order.getBuyer().getUserId();
        }
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof UUID uuid) {
                    return uuid;
                }
            }
        }
        return null;
    }

    private String extractUserEmailFallback(Object result, Object[] args) {
        if (result instanceof AuthenticationResult auth && auth.email() != null) {
            return auth.email();
        }
        if (result instanceof User user && user.getEmail() != null) {
            return user.getEmail().value();
        }
        if (result instanceof Order order && order.getBuyer() != null && order.getBuyer().getEmail() != null) {
            return order.getBuyer().getEmail().value();
        }
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof String str && str.contains("@")) {
                    return str;
                }
            }
        }
        return ANONYMOUS_EMAIL;
    }

    private String resolveClientIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            if (request.getRemoteAddr() != null && !request.getRemoteAddr().isBlank()) {
                return request.getRemoteAddr();
            }
        }
        return DEFAULT_IP;
    }

    private String buildDetails(ProceedingJoinPoint joinPoint, Throwable error) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        if (error == null) {
            return "Operación ejecutada exitosamente: " + className + "." + methodName;
        }
        return "Error en operación " + className + "." + methodName + ": "
                + error.getClass().getSimpleName() + " - " + error.getMessage();
    }

    private static String camelToUpperSnake(String input) {
        if (input == null || input.isEmpty()) {
            return "UNKNOWN_ACTION";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append('_');
            }
            sb.append(Character.toUpperCase(c));
        }
        return sb.toString();
    }
}

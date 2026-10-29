package application.adapters.aspect;

import application.domain.enums.AuditSeverity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a method (or all methods of a type) represents a critical business operation
 * that must be automatically audited to MongoDB by {@link AuditAspect}.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /**
     * Business action identifier (e.g. {@code "ORDER_CREATED"}, {@code "PAYMENT_PROCESSED"}).
     * If left blank, the aspect derives the action name from the intercepted method.
     */
    String action() default "";

    /**
     * Target domain resource name (e.g. {@code "Order"}, {@code "Product"}, {@code "Inventory"}).
     * If left blank, the aspect derives the resource name from the return type or target class.
     */
    String resource() default "";

    /**
     * Severity level recorded when the operation completes successfully.
     */
    AuditSeverity severity() default AuditSeverity.MEDIUM;
}

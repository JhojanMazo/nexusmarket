package application.adapters.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Enables AspectJ auto-proxying for cross-cutting concerns such as {@link application.adapters.aspect.AuditAspect}.
 */
@Configuration
@EnableAspectJAutoProxy
public class AopAuditConfig {
}

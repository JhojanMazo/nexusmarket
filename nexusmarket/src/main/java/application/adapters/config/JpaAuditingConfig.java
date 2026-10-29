package application.adapters.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing so {@link org.springframework.data.annotation.CreatedDate}
 * and {@link org.springframework.data.annotation.LastModifiedDate} fields on JPA entities
 * are automatically populated on insert and update.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}

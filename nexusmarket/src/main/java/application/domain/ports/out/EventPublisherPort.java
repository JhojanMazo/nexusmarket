package application.domain.ports.out;

/**
 * Outbound port for publishing domain events asynchronously or synchronously
 * without coupling domain/application services to a specific event bus infrastructure.
 */
public interface EventPublisherPort {
    void publish(Object event);
}

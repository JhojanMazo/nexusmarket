package application.adapters.events;

import application.domain.ports.out.EventPublisherPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter delegating domain event publication to Spring's {@link ApplicationEventPublisher}.
 */
@Component
public class SpringEventPublisherAdapter implements EventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(Object event) {
        if (event != null) {
            applicationEventPublisher.publishEvent(event);
        }
    }
}

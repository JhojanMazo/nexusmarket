package application.adapters.notification;

import application.domain.enums.NotificationChannel;
import application.domain.ports.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Enterprise notification adapter logging multi-channel notifications.
 */
@Component
public class NoOpNotificationAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpNotificationAdapter.class);

    @Override
    public void sendNotification(UUID recipientId, String subject, String message, NotificationChannel channel) {
        log.info("[NOTIFICATION] Channel: {} | Recipient: {} | Subject: {} | Message: {}",
                channel, recipientId, subject, message);
    }
}

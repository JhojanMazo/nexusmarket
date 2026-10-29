package application.adapters.notification;

import application.domain.enums.NotificationChannel;
import application.domain.ports.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Legacy no-op notification adapter superseded by {@link EmailNotificationServiceAdapter}.
 */
@Deprecated
public class NoOpNotificationAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpNotificationAdapter.class);

    @Override
    public void sendNotification(UUID recipientId, String subject, String message, NotificationChannel channel) {
        log.debug("[NO-OP NOTIFICATION] Channel: {} | Recipient: {} | Subject: {}", channel, recipientId, subject);
    }
}

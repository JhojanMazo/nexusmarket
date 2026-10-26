package application.domain.ports.out;

import application.domain.enums.NotificationChannel;

import java.util.UUID;

/**
 * Outbound port for dispatching multi-channel notifications to users and operators.
 */
public interface NotificationPort {
    void sendNotification(UUID recipientId, String subject, String message, NotificationChannel channel);
}

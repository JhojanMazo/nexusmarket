package application.adapters.notification;

import application.domain.enums.NotificationChannel;
import application.domain.ports.out.NotificationPort;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Enterprise email and multi-channel notification adapter replacing {@link NoOpNotificationAdapter}.
 * Formats structured HTML/text emails with full order and item breakdowns and records dispatched messages.
 */
@Primary
@Component
public class EmailNotificationServiceAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationServiceAdapter.class);

    public record SentNotification(
            UUID recipientId,
            String subject,
            String body,
            NotificationChannel channel,
            Instant sentAt
    ) {
    }

    private final List<SentNotification> sentNotifications = new CopyOnWriteArrayList<>();

    @Override
    public void sendNotification(UUID recipientId, String subject, String message, NotificationChannel channel) {
        NotificationChannel effectiveChannel = channel != null ? channel : NotificationChannel.EMAIL;
        String formattedMail = formatProfessionalEnvelope(recipientId, subject, message, effectiveChannel);
        sentNotifications.add(new SentNotification(recipientId, subject, formattedMail, effectiveChannel, Instant.now()));
        log.info("\n{}", formattedMail);
    }

    public static String formatOrderConfirmationBody(UUID orderId, List<OrderItem> items, Money totalAmount) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h2>Confirmación de Pedido #").append(orderId).append("</h2>");
        sb.append("<p>Su orden ha sido registrada exitosamente. Desglose de ítems:</p>");
        sb.append("<ul>");
        if (items != null) {
            for (OrderItem item : items) {
                sb.append("<li>")
                        .append(item.product().getName())
                        .append(" x ").append(item.quantity())
                        .append(" - Precio unitario: ").append(item.unitPrice().amount())
                        .append(" ").append(item.unitPrice().currency().getCurrencyCode())
                        .append(" (Subtotal: ").append(item.subtotal().amount()).append(")")
                        .append("</li>");
            }
        }
        sb.append("</ul>");
        if (totalAmount != null) {
            sb.append("<p><strong>Total Orden: ")
                    .append(totalAmount.amount()).append(" ")
                    .append(totalAmount.currency().getCurrencyCode())
                    .append("</strong></p>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private String formatProfessionalEnvelope(UUID recipientId, String subject, String body, NotificationChannel channel) {
        return """
                ================================================================================
                [NEXUSMARKET ENTERPRISE MAILER]
                Canal        : %s
                Destinatario : %s
                Asunto       : %s
                Fecha (UTC)  : %s
                --------------------------------------------------------------------------------
                %s
                ================================================================================
                """.formatted(channel, recipientId, subject, Instant.now(), body);
    }

    public List<SentNotification> getSentNotifications() {
        return Collections.unmodifiableList(sentNotifications);
    }

    public void clearSentNotifications() {
        sentNotifications.clear();
    }
}

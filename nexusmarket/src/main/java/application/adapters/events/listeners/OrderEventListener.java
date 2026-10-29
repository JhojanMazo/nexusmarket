package application.adapters.events.listeners;

import application.adapters.notification.EmailNotificationServiceAdapter;
import application.domain.enums.NotificationChannel;
import application.domain.enums.OrderStatus;
import application.domain.events.OrderCancelledEvent;
import application.domain.events.OrderPaidEvent;
import application.domain.events.OrderPlacedEvent;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.domain.ports.out.NotificationPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.billing.BillingAdministrationService;
import application.domain.valueobjects.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Asynchronous domain event listener reacting to order lifecycle events after transaction commit.
 */
@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final NotificationPort notificationPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final BillingAdministrationService billingAdministrationService;

    @Autowired
    public OrderEventListener(NotificationPort notificationPort,
                              OrderRepositoryPort orderRepositoryPort,
                              InvoiceRepositoryPort invoiceRepositoryPort,
                              BillingAdministrationService billingAdministrationService) {
        this.notificationPort = notificationPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.billingAdministrationService = billingAdministrationService;
    }

    public OrderEventListener(NotificationPort notificationPort) {
        this(notificationPort, null, null, new BillingAdministrationService());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleOrderPlaced(OrderPlacedEvent event) {
        if (event == null) {
            return;
        }
        log.info("[ASYNC EVENT] Processing OrderPlacedEvent for order {} on thread {}",
                event.orderId(), Thread.currentThread().getName());

        String subject = "Confirmación de Pedido #" + event.orderId();
        String body = EmailNotificationServiceAdapter.formatOrderConfirmationBody(
                event.orderId(),
                event.items(),
                event.totalAmount()
        );
        notificationPort.sendNotification(event.buyerId(), subject, body, NotificationChannel.EMAIL);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleOrderPaid(OrderPaidEvent event) {
        if (event == null) {
            return;
        }
        log.info("[ASYNC EVENT] Processing OrderPaidEvent for order {} on thread {}",
                event.orderId(), Thread.currentThread().getName());

        Invoice generatedInvoice = null;
        if (orderRepositoryPort != null && invoiceRepositoryPort != null) {
            Optional<Invoice> existingInvoice = invoiceRepositoryPort.findByOrderId(event.orderId());
            if (existingInvoice.isPresent()) {
                generatedInvoice = existingInvoice.get();
            } else {
                Optional<Order> orderOpt = orderRepositoryPort.findById(event.orderId());
                if (orderOpt.isPresent() && orderOpt.get().getStatus() != OrderStatus.CART) {
                    Order order = orderOpt.get();
                    Money zeroTax = new Money(BigDecimal.ZERO, order.subtotal().currency());
                    BillingAdministrationService service = billingAdministrationService != null
                            ? billingAdministrationService
                            : new BillingAdministrationService();
                    Invoice invoice = service.generateInvoice(order, zeroTax);
                    generatedInvoice = invoiceRepositoryPort.save(invoice);
                }
            }
        }

        String subject = "Pago Confirmado y Factura Emitida - Orden #" + event.orderId();
        String invoiceDetail = generatedInvoice != null
                ? "Factura #" + generatedInvoice.getInvoiceId() + " por total de "
                        + generatedInvoice.getTotal().amount() + " " + generatedInvoice.getTotal().currency().getCurrencyCode()
                : "Referencia de pago: " + event.paymentReference();
        String body = "<html><body><h2>Pago Confirmado</h2><p>Orden: " + event.orderId()
                + "</p><p>" + invoiceDetail + "</p></body></html>";

        notificationPort.sendNotification(event.buyerId(), subject, body, NotificationChannel.EMAIL);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        if (event == null) {
            return;
        }
        log.info("[ASYNC EVENT] Processing OrderCancelledEvent for order {} on thread {}",
                event.orderId(), Thread.currentThread().getName());

        if (orderRepositoryPort != null) {
            orderRepositoryPort.findById(event.orderId()).ifPresent(order -> {
                String subject = "Orden Cancelada #" + event.orderId();
                String body = "<html><body><h2>Orden Cancelada</h2><p>Motivo: " + event.reason()
                        + "</p><p>Ítems liberados: " + event.items().size() + "</p></body></html>";
                notificationPort.sendNotification(order.getBuyer().getUserId(), subject, body, NotificationChannel.EMAIL);
            });
        }
    }
}

package application.adapters.events;

import application.adapters.config.AsyncSchedulingConfig;
import application.adapters.events.listeners.OrderEventListener;
import application.adapters.notification.EmailNotificationServiceAdapter;
import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.NotificationChannel;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserStatus;
import application.domain.events.OrderPaidEvent;
import application.domain.events.OrderPlacedEvent;
import application.domain.models.Buyer;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.ports.out.EventPublisherPort;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.domain.ports.out.NotificationPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.billing.BillingAdministrationService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import application.domain.valueobjects.ProductVariant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that {@link OrderEventListener} processes {@link OrderPlacedEvent} and {@link OrderPaidEvent}
 * asynchronously on a separate thread and dispatches notifications / invoice generation.
 */
@SpringJUnitConfig(classes = OrderEventListenerTest.TestConfig.class)
class OrderEventListenerTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Configuration
    @Import({AsyncSchedulingConfig.class, SpringEventPublisherAdapter.class, OrderEventListener.class, BillingAdministrationService.class})
    static class TestConfig {
        @Bean
        NotificationPort notificationPort() {
            return mock(NotificationPort.class);
        }

        @Bean
        OrderRepositoryPort orderRepositoryPort() {
            return mock(OrderRepositoryPort.class);
        }

        @Bean
        InvoiceRepositoryPort invoiceRepositoryPort() {
            return mock(InvoiceRepositoryPort.class);
        }
    }

    @Autowired
    private EventPublisherPort eventPublisherPort;

    @Autowired
    private NotificationPort notificationPort;

    @Autowired
    private OrderRepositoryPort orderRepositoryPort;

    @Autowired
    private InvoiceRepositoryPort invoiceRepositoryPort;

    @Test
    void publishOrderPlacedEvent_invokesNotificationPortOnSeparateAsyncThread() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        Product product = new Product(
                UUID.randomUUID(),
                "Silla Ergonómica",
                new ProductVariant("Color", "Negro"),
                new Money(new BigDecimal("250.00"), USD),
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );
        OrderItem item = new OrderItem(product, 2, new Money(new BigDecimal("250.00"), USD));
        Money total = new Money(new BigDecimal("500.00"), USD);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> listenerThreadName = new AtomicReference<>();
        AtomicReference<String> capturedSubject = new AtomicReference<>();
        AtomicReference<String> capturedMessage = new AtomicReference<>();

        doAnswer(invocation -> {
            listenerThreadName.set(Thread.currentThread().getName());
            capturedSubject.set(invocation.getArgument(1));
            capturedMessage.set(invocation.getArgument(2));
            latch.countDown();
            return null;
        }).when(notificationPort).sendNotification(any(UUID.class), any(String.class), any(String.class), any(NotificationChannel.class));

        String callerThreadName = Thread.currentThread().getName();
        eventPublisherPort.publish(new OrderPlacedEvent(orderId, buyerId, total, List.of(item), Instant.now()));

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Async OrderPlacedEvent listener must complete within timeout");
        assertNotNull(listenerThreadName.get());
        assertNotEquals(callerThreadName, listenerThreadName.get(),
                "OrderPlacedEvent listener must execute on a separate asynchronous thread");
        assertTrue(capturedSubject.get().contains(orderId.toString()));
        assertTrue(capturedMessage.get().contains("Silla Ergonómica"));
        assertTrue(capturedMessage.get().contains("500.00"));
    }

    @Test
    void publishOrderPaidEvent_generatesInvoiceAndSendsNotificationAsynchronously() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        Buyer buyer = Buyer.reconstitute(
                buyerId,
                "Laura Martinez",
                new Email("laura@example.com"),
                new IdentityDocument("CC", "55667788"),
                new Address("Cra 15", "Bogota", "DC", "Colombia", "110111", null),
                List.of(),
                BuyerCommercialStatus.ENABLED,
                UserStatus.ACTIVE
        );
        Product product = new Product(
                UUID.randomUUID(),
                "Curso Cloud Architecture",
                new ProductVariant("Modality", "Online"),
                new Money(new BigDecimal("150.00"), USD),
                ProductStatus.PUBLISHED,
                ProductType.DIGITAL
        );
        Order paidOrder = Order.reconstitute(
                orderId,
                buyer,
                List.of(new OrderItem(product, 1, new Money(new BigDecimal("150.00"), USD))),
                buyer.getPrimaryAddress(),
                OrderStatus.PAID,
                LocalDate.now()
        );

        when(invoiceRepositoryPort.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(paidOrder));
        when(invoiceRepositoryPort.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(notificationPort).sendNotification(any(UUID.class), any(String.class), any(String.class), any(NotificationChannel.class));

        eventPublisherPort.publish(new OrderPaidEvent(orderId, buyerId, "CREDIT_CARD-" + orderId, Instant.now()));

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Async OrderPaidEvent listener must complete within timeout");
        verify(invoiceRepositoryPort).save(any(Invoice.class));
    }

    @Test
    void emailNotificationServiceAdapter_formatsAndRecordsStructuredEmail() {
        EmailNotificationServiceAdapter adapter = new EmailNotificationServiceAdapter();
        UUID recipientId = UUID.randomUUID();

        adapter.sendNotification(recipientId, "Asunto de Prueba", "<p>Detalle del pedido</p>", NotificationChannel.EMAIL);

        assertFalse(adapter.getSentNotifications().isEmpty());
        EmailNotificationServiceAdapter.SentNotification sent = adapter.getSentNotifications().get(0);
        assertEquals(recipientId, sent.recipientId());
        assertEquals("Asunto de Prueba", sent.subject());
        assertEquals(NotificationChannel.EMAIL, sent.channel());
        assertTrue(sent.body().contains("[NEXUSMARKET ENTERPRISE MAILER]"));
    }
}

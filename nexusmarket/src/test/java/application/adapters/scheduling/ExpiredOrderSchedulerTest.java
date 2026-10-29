package application.adapters.scheduling;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserStatus;
import application.domain.enums.WarehouseType;
import application.domain.events.OrderCancelledEvent;
import application.domain.models.Buyer;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Warehouse;
import application.domain.ports.out.EventPublisherPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.inventory.InventoryReservationService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import application.domain.valueobjects.ProductVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ExpiredOrderScheduler} and {@link application.domain.services.order.ExpiredOrderCleanupScheduler}.
 * Verifies that {@code PENDING_PAYMENT} orders older than the configured TTL (30 minutes)
 * transition to {@code CANCELLED}, release reserved stock back to available inventory,
 * and emit {@link OrderCancelledEvent}.
 */
@ExtendWith(MockitoExtension.class)
class ExpiredOrderSchedulerTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    private ExpiredOrderScheduler expiredOrderScheduler;

    private Buyer buyer;
    private Product physicalProduct;
    private Inventory inventoryLot;

    @BeforeEach
    void setUp() {
        expiredOrderScheduler = new ExpiredOrderScheduler(
                orderRepositoryPort,
                inventoryRepositoryPort,
                new InventoryReservationService(),
                eventPublisherPort,
                30L
        );

        Address address = new Address("Calle 100", "Bogota", "DC", "Colombia", "110111", null);
        buyer = Buyer.reconstitute(
                UUID.randomUUID(),
                "Andres Perez",
                new Email("andres@example.com"),
                new IdentityDocument("CC", "99887766"),
                address,
                List.of(),
                BuyerCommercialStatus.ENABLED,
                UserStatus.ACTIVE
        );

        physicalProduct = new Product(
                UUID.randomUUID(),
                "Tarjeta Gráfica RTX",
                new ProductVariant("VRAM", "16GB"),
                new Money(new BigDecimal("800.00"), USD),
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );

        Warehouse warehouse = new Warehouse(
                UUID.randomUUID(),
                "Bodega Principal",
                address,
                WarehouseType.MARKETPLACE,
                null,
                500.0
        );
        inventoryLot = new Inventory(UUID.randomUUID(), physicalProduct, warehouse, 10);
        // Simulate 3 units reserved for the pending order (available = 7, reserved = 3)
        inventoryLot.reserve(3);
    }

    @Test
    void releaseExpiredOrders_cancelsPendingOrderOlderThan30MinutesAndRestoresInventory() {
        Order expiredOrder = Order.reconstitute(
                UUID.randomUUID(),
                buyer,
                List.of(new OrderItem(physicalProduct, 3, new Money(new BigDecimal("800.00"), USD))),
                buyer.getPrimaryAddress(),
                OrderStatus.PENDING_PAYMENT,
                LocalDate.now(),
                LocalDateTime.now().minusMinutes(45)
        );

        Order recentOrder = Order.reconstitute(
                UUID.randomUUID(),
                buyer,
                List.of(new OrderItem(physicalProduct, 1, new Money(new BigDecimal("800.00"), USD))),
                buyer.getPrimaryAddress(),
                OrderStatus.PENDING_PAYMENT,
                LocalDate.now(),
                LocalDateTime.now().minusMinutes(10)
        );

        when(orderRepositoryPort.findAll()).thenReturn(List.of(expiredOrder, recentOrder));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryRepositoryPort.findByProductId(physicalProduct.getProductId())).thenReturn(List.of(inventoryLot));
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Order> cancelled = expiredOrderScheduler.releaseExpiredOrders();

        assertEquals(1, cancelled.size());
        assertEquals(expiredOrder.getOrderId(), cancelled.get(0).getOrderId());
        assertEquals(OrderStatus.CANCELLED, expiredOrder.getStatus(), "Expired order must transition to CANCELLED");
        assertEquals(OrderStatus.PENDING_PAYMENT, recentOrder.getStatus(), "Recent order must remain PENDING_PAYMENT");

        // Verify inventory stock restoration: available goes from 7 back to 10, reserved goes from 3 to 0
        assertEquals(10, inventoryLot.getAvailableQuantity(), "Reserved units must be returned to available stock");
        assertEquals(0, inventoryLot.getReservedQuantity(), "Reserved quantity must be decremented");
        verify(inventoryRepositoryPort, times(1)).save(inventoryLot);
        verify(orderRepositoryPort, times(1)).save(expiredOrder);

        ArgumentCaptor<OrderCancelledEvent> eventCaptor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(eventPublisherPort, times(1)).publish(eventCaptor.capture());
        OrderCancelledEvent publishedEvent = eventCaptor.getValue();
        assertEquals(expiredOrder.getOrderId(), publishedEvent.orderId());
        assertEquals(1, publishedEvent.items().size());
        assertTrue(publishedEvent.reason().contains("30"));
    }

    @Test
    void releaseExpiredOrders_ignoresPaidOrdersEvenIfCreatedLongAgo() {
        Order paidOrder = Order.reconstitute(
                UUID.randomUUID(),
                buyer,
                List.of(new OrderItem(physicalProduct, 2, new Money(new BigDecimal("800.00"), USD))),
                buyer.getPrimaryAddress(),
                OrderStatus.PAID,
                LocalDate.now().minusDays(2),
                LocalDateTime.now().minusDays(2)
        );

        when(orderRepositoryPort.findAll()).thenReturn(List.of(paidOrder));

        List<Order> cancelled = expiredOrderScheduler.releaseExpiredOrders();

        assertTrue(cancelled.isEmpty());
        assertEquals(OrderStatus.PAID, paidOrder.getStatus());
        verify(orderRepositoryPort, never()).save(any());
        verify(inventoryRepositoryPort, never()).save(any());
        verify(eventPublisherPort, never()).publish(any());
    }
}

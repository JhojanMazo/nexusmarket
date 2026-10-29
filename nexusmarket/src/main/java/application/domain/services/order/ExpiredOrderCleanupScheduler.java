package application.domain.services.order;

import application.domain.enums.OrderStatus;
import application.domain.enums.ProductType;
import application.domain.events.OrderCancelledEvent;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.ports.out.EventPublisherPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.inventory.InventoryReservationService;
import application.domain.valueobjects.OrderItem;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain service that identifies {@link OrderStatus#PENDING_PAYMENT} orders exceeding the configured
 * TTL, cancels them, restores reserved stock to available inventory, and publishes {@link OrderCancelledEvent}.
 */
public class ExpiredOrderCleanupScheduler {

    private final OrderRepositoryPort orderRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final InventoryReservationService inventoryReservationService;
    private final EventPublisherPort eventPublisherPort;
    private final long ttlMinutes;

    public ExpiredOrderCleanupScheduler(
            OrderRepositoryPort orderRepositoryPort,
            InventoryRepositoryPort inventoryRepositoryPort,
            InventoryReservationService inventoryReservationService,
            EventPublisherPort eventPublisherPort,
            long ttlMinutes) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.inventoryRepositoryPort = inventoryRepositoryPort;
        this.inventoryReservationService = inventoryReservationService != null
                ? inventoryReservationService
                : new InventoryReservationService();
        this.eventPublisherPort = eventPublisherPort;
        this.ttlMinutes = ttlMinutes > 0 ? ttlMinutes : 30L;
    }

    public ExpiredOrderCleanupScheduler(
            OrderRepositoryPort orderRepositoryPort,
            InventoryRepositoryPort inventoryRepositoryPort,
            InventoryReservationService inventoryReservationService,
            EventPublisherPort eventPublisherPort) {
        this(orderRepositoryPort, inventoryRepositoryPort, inventoryReservationService, eventPublisherPort, 30L);
    }

    /**
     * Scans all {@code PENDING_PAYMENT} orders created more than {@code ttlMinutes} ago,
     * transitions them to {@code CANCELLED}, releases their item quantities back to inventory,
     * publishes {@link OrderCancelledEvent}, and persists the updated order.
     *
     * @return list of cancelled orders processed during this execution
     */
    public List<Order> cleanupExpiredOrders() {
        if (orderRepositoryPort == null) {
            return List.of();
        }
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(ttlMinutes);
        List<Order> cancelledOrders = new ArrayList<>();

        for (Order order : orderRepositoryPort.findAll()) {
            if (isExpiredPendingOrder(order, cutoff)) {
                order.cancel();
                releaseOrderItemsStock(order);
                Order saved = orderRepositoryPort.save(order);
                if (eventPublisherPort != null) {
                    String reason = "Orden expirada tras superar el tiempo límite de pago (" + ttlMinutes + " minutos)";
                    eventPublisherPort.publish(new OrderCancelledEvent(
                            saved.getOrderId(),
                            reason,
                            saved.getItems(),
                            Instant.now()
                    ));
                }
                cancelledOrders.add(saved);
            }
        }
        return cancelledOrders;
    }

    private boolean isExpiredPendingOrder(Order order, LocalDateTime cutoff) {
        if (order == null || order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            return false;
        }
        LocalDateTime orderCreatedAt = order.getCreatedAt();
        return orderCreatedAt != null && orderCreatedAt.isBefore(cutoff);
    }

    private void releaseOrderItemsStock(Order order) {
        if (inventoryRepositoryPort == null || order.getItems() == null) {
            return;
        }
        for (OrderItem item : order.getItems()) {
            if (item.product() != null && item.product().getType() == ProductType.PHYSICAL) {
                List<Inventory> lots = inventoryRepositoryPort.findByProductId(item.product().getProductId());
                if (lots != null && !lots.isEmpty()) {
                    Inventory targetLot = lots.stream()
                            .filter(lot -> lot.getReservedQuantity() >= item.quantity())
                            .findFirst()
                            .orElseGet(() -> lots.get(0));
                    inventoryReservationService.releaseReservation(targetLot, item.quantity());
                    inventoryRepositoryPort.save(targetLot);
                }
            }
        }
    }
}

package application.domain.services.order;

import application.domain.enums.OrderStatus;
import application.domain.models.Order;
import java.util.Objects;

/**
 * 5.4 Coordinates order lifecycle status transitions (FR-017, FR-018).
 */
public class OrderStatusMonitoringService {

    public void updateStatus(Order order, OrderStatus newStatus) {
        Objects.requireNonNull(order, "Order must not be null");
        Objects.requireNonNull(newStatus, "New status must not be null");
        order.advanceTo(newStatus);
    }
}

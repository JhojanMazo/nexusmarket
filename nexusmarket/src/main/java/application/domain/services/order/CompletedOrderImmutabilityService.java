package application.domain.services.order;

import application.domain.enums.OrderStatus;
import application.domain.models.Order;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 5.5 Guarantees audit compliance and data integrity for finalized orders.
 */
@Service
public class CompletedOrderImmutabilityService {

    public void ensureImmutability(Order order) {
        Objects.requireNonNull(order, "Order must not be null");
        if (order.getStatus() == OrderStatus.DELIVERED_COMPLETED) {
            throw new IllegalStateException("La orden se encuentra en estado DELIVERED_COMPLETED y es estrictamente inmutable");
        }
    }
}

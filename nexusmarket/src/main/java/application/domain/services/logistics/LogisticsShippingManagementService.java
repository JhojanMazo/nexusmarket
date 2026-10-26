package application.domain.services.logistics;

import application.domain.enums.OrderStatus;
import application.domain.models.Order;
import application.domain.models.Shipment;
import application.domain.models.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 6.2 Coordinates physical fulfillment with logistics operators (FR-021, FR-022).
 */
@Service
public class LogisticsShippingManagementService {

    public Shipment coordinateShipping(User logisticsOperator, Order order) {
        Objects.requireNonNull(logisticsOperator, "Logistics operator must not be null");
        Objects.requireNonNull(order, "Order must not be null");

        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Solo se puede coordinar el despacho de órdenes con estado PAID");
        }

        Shipment shipment = Shipment.create(logisticsOperator, order);
        shipment.registerDispatch();
        order.advanceTo(OrderStatus.SHIPPED);

        return shipment;
    }
}

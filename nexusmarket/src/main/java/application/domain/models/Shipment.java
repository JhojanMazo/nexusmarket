package application.domain.models;

import application.domain.enums.UserRole;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain object representing a shipment.
 *
 * FR-021: Shipment creation must be performed by a user with LOGISTICS_OPERATOR role.
 * FR-022: Delivery confirmation is only allowed after the shipment has been dispatched.
 */
public class Shipment {
    public enum ShipmentStatus { CREATED, DISPATCHED, DELIVERED }

    private final UUID shipmentId;
    private final Order order;
    private final User logisticsOperator;
    private ShipmentStatus status;

    private Shipment(UUID shipmentId, Order order, User logisticsOperator) {
        this.shipmentId = Objects.requireNonNull(shipmentId);
        this.order = Objects.requireNonNull(order);
        this.logisticsOperator = Objects.requireNonNull(logisticsOperator);
        if (logisticsOperator.getRole() != UserRole.LOGISTICS_OPERATOR) {
            throw new IllegalArgumentException("User must have LOGISTICS_OPERATOR role to create a Shipment");
        }
        this.status = ShipmentStatus.CREATED;
    }

    /** Factory method ensuring role validation. */
    public static Shipment create(User logisticsOperator, Order order) {
        Objects.requireNonNull(logisticsOperator);
        Objects.requireNonNull(order);
        return new Shipment(UUID.randomUUID(), order, logisticsOperator);
    }

    public static Shipment reconstitute(UUID shipmentId, Order order, User logisticsOperator, ShipmentStatus status) {
        Shipment shipment = new Shipment(shipmentId, order, logisticsOperator);
        shipment.status = Objects.requireNonNull(status);
        return shipment;
    }

    public UUID getShipmentId() { return shipmentId; }
    public Order getOrder() { return order; }
    public User getLogisticsOperator() { return logisticsOperator; }
    public ShipmentStatus getStatus() { return status; }

    /** Register the dispatch of the shipment. Must be in CREATED state. */
    public void registerDispatch() {
        if (status != ShipmentStatus.CREATED) {
            throw new IllegalStateException("Shipment can only be dispatched from CREATED state");
        }
        this.status = ShipmentStatus.DISPATCHED;
    }

    /** Confirm delivery. Must be in DISPATCHED state. */
    public void confirmDelivery() {
        if (status != ShipmentStatus.DISPATCHED) {
            throw new IllegalStateException("Delivery can only be confirmed after dispatch");
        }
        this.status = ShipmentStatus.DELIVERED;
    }
}

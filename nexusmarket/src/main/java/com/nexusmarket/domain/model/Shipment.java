package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.UserRole;
import com.nexusmarket.domain.valueobject.Address;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Logistics process for dispatching and transporting a physical Order.
 *
 * <p>{@code logisticsOperator} is typed as {@link User} rather than a
 * dedicated "LogisticsOperator" subclass — see the design note on
 * {@link User} — so the LOGISTICS_OPERATOR role is enforced here at
 * construction time instead of by the type system.
 */
public class Shipment {

    private final String shipmentId;
    private final Order order;
    private final User logisticsOperator;
    private LocalDate dispatchDate;  // set when dispatched
    private LocalDate deliveryDate;  // set when delivery is confirmed
    private final Address deliveryAddress;

    public Shipment(String shipmentId, Order order, User logisticsOperator, Address deliveryAddress) {
        this.shipmentId = requireNotBlank(shipmentId, "shipmentId");
        this.order = Objects.requireNonNull(order, "order is required");
        this.logisticsOperator = Objects.requireNonNull(logisticsOperator, "logisticsOperator is required");
        if (logisticsOperator.getRole() != UserRole.LOGISTICS_OPERATOR) {
            throw new IllegalArgumentException("The assigned user must have the LOGISTICS_OPERATOR role.");
        }
        this.deliveryAddress = Objects.requireNonNull(deliveryAddress, "deliveryAddress is required");
    }

    private static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be blank.");
        }
        return value;
    }

    public void registerDispatch(LocalDate date) {
        this.dispatchDate = Objects.requireNonNull(date, "dispatch date is required");
    }

    public void confirmDelivery(LocalDate date) {
        if (dispatchDate == null) {
            throw new IllegalStateException("Cannot confirm delivery for a shipment that has not been dispatched.");
        }
        this.deliveryDate = Objects.requireNonNull(date, "delivery date is required");
    }

    public String getShipmentId() { return shipmentId; }
    public Order getOrder() { return order; }
    public User getLogisticsOperator() { return logisticsOperator; }
    public Optional<LocalDate> getDispatchDate() { return Optional.ofNullable(dispatchDate); }
    public Optional<LocalDate> getDeliveryDate() { return Optional.ofNullable(deliveryDate); }
    public Address getDeliveryAddress() { return deliveryAddress; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shipment)) return false;
        return shipmentId.equals(((Shipment) o).shipmentId);
    }

    @Override
    public int hashCode() { return Objects.hash(shipmentId); }
}

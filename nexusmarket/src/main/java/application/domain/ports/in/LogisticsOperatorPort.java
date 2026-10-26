package application.domain.ports.in;

import application.domain.enums.InventoryMovementType;
import application.domain.enums.InventoryStatus;
import application.domain.enums.OrderStatus;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.Shipment;

import java.util.List;
import java.util.UUID;

public interface LogisticsOperatorPort {
    Order updateDeliveryStatus(UUID orderId, OrderStatus status);
    Order advanceOrderStatus(UUID orderId, OrderStatus nextStatus);
    InventoryMovement registerMovement(UUID inventoryId, int quantity, String movementType, String reason);
    void reserveInventory(UUID productId, int quantity);
    void releaseInventory(UUID productId, int quantity);

    Shipment dispatchOrder(UUID orderId, UUID logisticsOperatorId);
    Shipment confirmDelivery(UUID shipmentId);
    Shipment getShipmentById(UUID shipmentId);
    Shipment getShipmentByOrderId(UUID orderId);
    List<Shipment> listShipments();

    Inventory updateInventoryStatus(UUID inventoryId, InventoryStatus status);
    InventoryMovement registerInventoryMovement(UUID inventoryId, int quantity, InventoryMovementType type, String reference);
    List<InventoryMovement> getInventoryMovements(UUID inventoryId);
}

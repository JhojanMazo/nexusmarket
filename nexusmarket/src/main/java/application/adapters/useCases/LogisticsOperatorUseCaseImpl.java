package application.adapters.useCases;

import application.adapters.aspect.Auditable;
import application.domain.enums.AuditSeverity;
import application.domain.enums.InventoryMovementType;
import application.domain.enums.InventoryStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.ports.in.LogisticsOperatorPort;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.logistics.LogisticsShippingManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class LogisticsOperatorUseCaseImpl implements LogisticsOperatorPort {

    private final OrderRepositoryPort orderRepository;
    private final ShipmentRepositoryPort shipmentRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final InventoryMovementRepositoryPort inventoryMovementRepository;
    private final UserRepositoryPort userRepository;
    private final LogisticsShippingManagementService logisticsShippingManagementService;

    @Autowired
    public LogisticsOperatorUseCaseImpl(OrderRepositoryPort orderRepository,
                                        ShipmentRepositoryPort shipmentRepository,
                                        InventoryRepositoryPort inventoryRepository,
                                        InventoryMovementRepositoryPort inventoryMovementRepository,
                                        UserRepositoryPort userRepository,
                                        LogisticsShippingManagementService logisticsShippingManagementService) {
        this.orderRepository = orderRepository;
        this.shipmentRepository = shipmentRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.userRepository = userRepository;
        this.logisticsShippingManagementService = logisticsShippingManagementService;
    }

    public LogisticsOperatorUseCaseImpl(OrderRepositoryPort orderRepository) {
        this(orderRepository, null, null, null, null, new LogisticsShippingManagementService());
    }

    @Override
    @Transactional
    public Order updateDeliveryStatus(UUID orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", orderId));
        order.advanceTo(status);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order advanceOrderStatus(UUID orderId, OrderStatus nextStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", orderId));
        order.advanceTo(nextStatus);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    @Auditable(action = "ORDER_DISPATCHED", resource = "Shipment", severity = AuditSeverity.MEDIUM)
    public Shipment dispatchOrder(UUID orderId, UUID logisticsOperatorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", orderId));
        User operator = userRepository.findById(logisticsOperatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Operador Logístico", logisticsOperatorId));
        if (operator.getRole() != UserRole.LOGISTICS_OPERATOR) {
            throw new IllegalArgumentException("El usuario debe tener rol LOGISTICS_OPERATOR (FR-021)");
        }

        Shipment shipment = logisticsShippingManagementService.coordinateShipping(operator, order);
        orderRepository.save(order);
        return shipmentRepository.save(shipment);
    }

    @Override
    @Transactional
    @Auditable(action = "SHIPMENT_DELIVERY_CONFIRMED", resource = "Shipment", severity = AuditSeverity.HIGH)
    public Shipment confirmDelivery(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Envío", shipmentId));
        shipment.confirmDelivery();
        shipment.getOrder().advanceTo(OrderStatus.DELIVERED_COMPLETED);
        orderRepository.save(shipment.getOrder());
        return shipmentRepository.save(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public Shipment getShipmentById(UUID shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Envío", shipmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Shipment getShipmentByOrderId(UUID orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Envío para la orden", orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipment> listShipments() {
        return shipmentRepository.findAll();
    }

    @Override
    @Transactional
    public Inventory updateInventoryStatus(UUID inventoryId, InventoryStatus status) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario", inventoryId));
        inventory.setStatus(status);
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public InventoryMovement registerInventoryMovement(UUID inventoryId, int quantity, InventoryMovementType type, String reference) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario", inventoryId));

        switch (type) {
            case INBOUND -> inventory.registerEntry(quantity, reference);
            case RESERVATION -> inventory.reserve(quantity);
            case SALE_OUTBOUND -> inventory.confirmSaleOutbound(quantity);
            case RETURN -> inventory.restockFromReturn(quantity);
            case ADJUSTMENT -> inventory.adjustStock(quantity, reference);
        }
        inventoryRepository.save(inventory);

        List<InventoryMovement> movements = inventory.getMovements();
        InventoryMovement recorded = movements.get(movements.size() - 1);
        inventoryMovementRepository.save(recorded);
        return recorded;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovement> getInventoryMovements(UUID inventoryId) {
        return inventoryMovementRepository.findByInventoryId(inventoryId);
    }

    @Override
    @Transactional
    public InventoryMovement registerMovement(UUID inventoryId, int quantity, String movementType, String reason) {
        InventoryMovementType type;
        try {
            type = movementType != null ? InventoryMovementType.valueOf(movementType.toUpperCase()) : InventoryMovementType.ADJUSTMENT;
        } catch (IllegalArgumentException e) {
            type = InventoryMovementType.ADJUSTMENT;
        }
        int validQuantity = quantity != 0 ? quantity : 1;
        return new InventoryMovement(
                UUID.randomUUID(),
                inventoryId != null ? inventoryId : UUID.randomUUID(),
                type,
                validQuantity,
                Instant.now(),
                reason != null ? reason : "Manual logistics adjustment"
        );
    }

    @Override
    @Transactional
    public void reserveInventory(UUID productId, int quantity) {
        List<Inventory> list = inventoryRepository.findByProductId(productId);
        if (!list.isEmpty()) {
            Inventory inv = list.get(0);
            inv.reserve(quantity);
            inventoryRepository.save(inv);
        }
    }

    @Override
    @Transactional
    public void releaseInventory(UUID productId, int quantity) {
        List<Inventory> list = inventoryRepository.findByProductId(productId);
        if (!list.isEmpty()) {
            Inventory inv = list.get(0);
            inv.registerEntry(quantity, "Release inventory");
            inventoryRepository.save(inv);
        }
    }
}

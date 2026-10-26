package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.CreateShipmentRequestDTO;
import application.adapters.rest.dtos.requests.InventoryMovementRequestDTO;
import application.adapters.rest.dtos.requests.UpdateOrderStatusRequestDTO;
import application.adapters.rest.dtos.responses.InventoryMovementResponseDTO;
import application.adapters.rest.dtos.responses.InventoryResponseDTO;
import application.adapters.rest.dtos.responses.OrderResponseDTO;
import application.adapters.rest.dtos.responses.ShipmentResponseDTO;
import application.adapters.rest.mappers.InventoryRestMapper;
import application.adapters.rest.mappers.OrderRestMapper;
import application.adapters.rest.mappers.ShipmentRestMapper;
import application.domain.enums.InventoryStatus;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.Shipment;
import application.domain.ports.in.LogisticsOperatorPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/logistics")
public class LogisticsOperatorRestController {

    private final LogisticsOperatorPort logisticsPort;
    private final OrderRestMapper orderRestMapper;
    private final ShipmentRestMapper shipmentRestMapper;
    private final InventoryRestMapper inventoryRestMapper;

    @Autowired
    public LogisticsOperatorRestController(LogisticsOperatorPort logisticsPort,
                                           OrderRestMapper orderRestMapper,
                                           ShipmentRestMapper shipmentRestMapper,
                                           InventoryRestMapper inventoryRestMapper) {
        this.logisticsPort = logisticsPort;
        this.orderRestMapper = orderRestMapper;
        this.shipmentRestMapper = shipmentRestMapper;
        this.inventoryRestMapper = inventoryRestMapper;
    }

    public LogisticsOperatorRestController(LogisticsOperatorPort logisticsPort,
                                           OrderRestMapper orderRestMapper) {
        this(logisticsPort, orderRestMapper, new ShipmentRestMapper(), new InventoryRestMapper());
    }

    @PostMapping("/shipments/dispatch")
    public ResponseEntity<ShipmentResponseDTO> dispatchOrder(@RequestBody CreateShipmentRequestDTO request) {
        Shipment shipment = logisticsPort.dispatchOrder(request.orderId(), request.logisticsOperatorId());
        return ResponseEntity.status(HttpStatus.CREATED).body(shipmentRestMapper.toResponse(shipment));
    }

    @PatchMapping("/shipments/{shipmentId}/deliver")
    public ResponseEntity<ShipmentResponseDTO> confirmDelivery(@PathVariable("shipmentId") UUID shipmentId) {
        Shipment shipment = logisticsPort.confirmDelivery(shipmentId);
        return ResponseEntity.ok(shipmentRestMapper.toResponse(shipment));
    }

    @GetMapping("/shipments")
    public ResponseEntity<List<ShipmentResponseDTO>> listShipments() {
        List<Shipment> shipments = logisticsPort.listShipments();
        return ResponseEntity.ok(shipmentRestMapper.toResponseList(shipments));
    }

    @GetMapping("/shipments/{shipmentId}")
    public ResponseEntity<ShipmentResponseDTO> getShipmentById(@PathVariable("shipmentId") UUID shipmentId) {
        Shipment shipment = logisticsPort.getShipmentById(shipmentId);
        return ResponseEntity.ok(shipmentRestMapper.toResponse(shipment));
    }

    @GetMapping("/shipments/order/{orderId}")
    public ResponseEntity<ShipmentResponseDTO> getShipmentByOrderId(@PathVariable("orderId") UUID orderId) {
        Shipment shipment = logisticsPort.getShipmentByOrderId(orderId);
        return ResponseEntity.ok(shipmentRestMapper.toResponse(shipment));
    }

    @PatchMapping("/inventory/{inventoryId}/status")
    public ResponseEntity<InventoryResponseDTO> updateInventoryStatus(
            @PathVariable("inventoryId") UUID inventoryId,
            @RequestBody Map<String, String> body) {
        InventoryStatus status = InventoryStatus.valueOf(body.get("status"));
        Inventory updated = logisticsPort.updateInventoryStatus(inventoryId, status);
        return ResponseEntity.ok(inventoryRestMapper.toResponse(updated));
    }

    @PostMapping("/inventory/{inventoryId}/movement")
    public ResponseEntity<InventoryMovementResponseDTO> registerInventoryMovement(
            @PathVariable("inventoryId") UUID inventoryId,
            @RequestBody InventoryMovementRequestDTO request) {
        InventoryMovement movement = logisticsPort.registerInventoryMovement(
                inventoryId,
                request.quantity(),
                request.type(),
                request.reference()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryRestMapper.toMovementResponse(movement));
    }

    @GetMapping("/inventory/{inventoryId}/movements")
    public ResponseEntity<List<InventoryMovementResponseDTO>> getInventoryMovements(
            @PathVariable("inventoryId") UUID inventoryId) {
        List<InventoryMovement> movements = logisticsPort.getInventoryMovements(inventoryId);
        return ResponseEntity.ok(inventoryRestMapper.toMovementResponseList(movements));
    }

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateDeliveryStatus(@PathVariable("orderId") UUID orderId,
                                                                 @RequestBody UpdateOrderStatusRequestDTO request) {
        Order updated = logisticsPort.updateDeliveryStatus(orderId, request.status());
        return ResponseEntity.ok(orderRestMapper.toResponse(updated));
    }

    @PostMapping("/orders/{orderId}/advance")
    public ResponseEntity<OrderResponseDTO> advanceOrderStatus(@PathVariable("orderId") UUID orderId,
                                                              @RequestBody UpdateOrderStatusRequestDTO request) {
        Order updated = logisticsPort.advanceOrderStatus(orderId, request.status());
        return ResponseEntity.ok(orderRestMapper.toResponse(updated));
    }

    @PostMapping("/inventory/{inventoryId}/movements")
    public ResponseEntity<InventoryMovement> registerMovement(
            @PathVariable("inventoryId") UUID inventoryId,
            @RequestBody Map<String, Object> body) {
        int quantity = 1;
        if (body.get("quantity") != null) {
            quantity = Integer.parseInt(body.get("quantity").toString());
        }
        String type = (String) body.getOrDefault("type", "ADJUSTMENT");
        String reason = (String) body.getOrDefault("reason", "Manual adjustment");

        InventoryMovement movement = logisticsPort.registerMovement(inventoryId, quantity, type, reason);
        return ResponseEntity.status(HttpStatus.CREATED).body(movement);
    }

    @PostMapping("/inventory/reserve")
    public ResponseEntity<Map<String, Object>> reserveStock(
            @RequestParam("productId") UUID productId,
            @RequestParam("quantity") int quantity) {
        logisticsPort.reserveInventory(productId, quantity);
        return ResponseEntity.ok(Map.of("productId", productId, "reservedQuantity", quantity, "status", "RESERVED"));
    }

    @PostMapping("/inventory/release")
    public ResponseEntity<Map<String, Object>> releaseStock(
            @RequestParam("productId") UUID productId,
            @RequestParam("quantity") int quantity) {
        logisticsPort.releaseInventory(productId, quantity);
        return ResponseEntity.ok(Map.of("productId", productId, "releasedQuantity", quantity, "status", "RELEASED"));
    }
}

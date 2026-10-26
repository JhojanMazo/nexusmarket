package application.adapters.rest.controllers;

import application.adapters.rest.dtos.responses.AdministrativeReportResponseDTO;
import application.adapters.rest.dtos.responses.InventoryResponseDTO;
import application.adapters.rest.dtos.responses.OperationResponseDTO;
import application.adapters.rest.dtos.responses.OrderResponseDTO;
import application.adapters.rest.dtos.responses.RefundResponseDTO;
import application.adapters.rest.dtos.responses.ReturnRequestResponseDTO;
import application.adapters.rest.dtos.responses.ShipmentResponseDTO;
import application.adapters.rest.dtos.responses.WarehouseResponseDTO;
import application.adapters.rest.mappers.InventoryRestMapper;
import application.adapters.rest.mappers.OperationRestMapper;
import application.adapters.rest.mappers.OrderRestMapper;
import application.adapters.rest.mappers.RefundRestMapper;
import application.adapters.rest.mappers.ReturnRequestRestMapper;
import application.adapters.rest.mappers.ShipmentRestMapper;
import application.adapters.rest.mappers.WarehouseRestMapper;
import application.domain.enums.ApprovalDecision;
import application.domain.models.AdministrativeReport;
import application.domain.models.Inventory;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.models.Warehouse;
import application.domain.ports.in.SupervisorPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/supervisors")
public class SupervisorRestController {

    private final SupervisorPort supervisorPort;
    private final OrderRestMapper orderRestMapper;
    private final OperationRestMapper operationRestMapper;
    private final WarehouseRestMapper warehouseRestMapper;
    private final InventoryRestMapper inventoryRestMapper;
    private final ShipmentRestMapper shipmentRestMapper;
    private final ReturnRequestRestMapper returnRequestRestMapper;
    private final RefundRestMapper refundRestMapper;

    @Autowired
    public SupervisorRestController(SupervisorPort supervisorPort,
                                    OrderRestMapper orderRestMapper,
                                    OperationRestMapper operationRestMapper,
                                    WarehouseRestMapper warehouseRestMapper,
                                    InventoryRestMapper inventoryRestMapper,
                                    ShipmentRestMapper shipmentRestMapper,
                                    ReturnRequestRestMapper returnRequestRestMapper,
                                    RefundRestMapper refundRestMapper) {
        this.supervisorPort = supervisorPort;
        this.orderRestMapper = orderRestMapper;
        this.operationRestMapper = operationRestMapper;
        this.warehouseRestMapper = warehouseRestMapper;
        this.inventoryRestMapper = inventoryRestMapper;
        this.shipmentRestMapper = shipmentRestMapper;
        this.returnRequestRestMapper = returnRequestRestMapper;
        this.refundRestMapper = refundRestMapper;
    }

    public SupervisorRestController(SupervisorPort supervisorPort,
                                    OrderRestMapper orderRestMapper,
                                    OperationRestMapper operationRestMapper) {
        this(supervisorPort, orderRestMapper, operationRestMapper,
                new WarehouseRestMapper(), new InventoryRestMapper(), new ShipmentRestMapper(),
                new ReturnRequestRestMapper(), new RefundRestMapper());
    }

    @PostMapping("/disputes/{disputeId}/review")
    public ResponseEntity<Map<String, String>> reviewDispute(
            @PathVariable("disputeId") UUID disputeId,
            @RequestParam("decision") ApprovalDecision decision,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.getOrDefault("notes", "") : "";
        supervisorPort.reviewDispute(disputeId, decision, notes);
        return ResponseEntity.ok(Map.of("message", "Dispute reviewed successfully", "decision", decision.name()));
    }

    @PostMapping("/returns/{returnId}/approve")
    public ResponseEntity<Map<String, Object>> approveReturn(
            @PathVariable("returnId") UUID returnId,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.getOrDefault("notes", "Approved by supervisor") : "Approved by supervisor";
        ReturnRequest request = supervisorPort.approveReturn(returnId, notes);
        return ResponseEntity.ok(Map.of(
                "returnId", returnId,
                "status", request != null ? request.getStatus().name() : "APPROVED"
        ));
    }

    @PostMapping("/returns/{returnId}/reject")
    public ResponseEntity<Map<String, Object>> rejectReturn(
            @PathVariable("returnId") UUID returnId,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.getOrDefault("notes", "Rejected by supervisor") : "Rejected by supervisor";
        ReturnRequest request = supervisorPort.rejectReturn(returnId, notes);
        return ResponseEntity.ok(Map.of(
                "returnId", returnId,
                "status", request != null ? request.getStatus().name() : "REJECTED"
        ));
    }

    @PostMapping("/returns/{returnId}/refund")
    public ResponseEntity<Map<String, Object>> processRefund(
            @PathVariable("returnId") UUID returnId,
            @RequestBody(required = false) Map<String, Object> body) {
        BigDecimal amount = BigDecimal.TEN;
        String reason = "Supervisor authorized refund";
        if (body != null) {
            if (body.get("amount") != null) {
                amount = new BigDecimal(body.get("amount").toString());
            }
            if (body.get("reason") != null) {
                reason = body.get("reason").toString();
            }
        }
        Refund refund = supervisorPort.processRefund(returnId, amount, reason);
        return ResponseEntity.ok(Map.of(
                "refundId", refund != null ? refund.getRefundId() : UUID.randomUUID(),
                "status", refund != null ? refund.getStatus().name() : "PROCESSED"
        ));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<Order> orders = supervisorPort.getAllOrders();
        return ResponseEntity.ok(orderRestMapper.toResponseList(orders));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable("orderId") UUID orderId) {
        Order order = supervisorPort.getOrderById(orderId);
        return ResponseEntity.ok(orderRestMapper.toResponse(order));
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponseDTO>> getAllWarehouses() {
        List<Warehouse> warehouses = supervisorPort.getAllWarehouses();
        return ResponseEntity.ok(warehouseRestMapper.toResponseList(warehouses));
    }

    @GetMapping("/inventories")
    public ResponseEntity<List<InventoryResponseDTO>> getAllInventories() {
        List<Inventory> inventories = supervisorPort.getAllInventories();
        return ResponseEntity.ok(inventoryRestMapper.toResponseList(inventories));
    }

    @GetMapping("/shipments")
    public ResponseEntity<List<ShipmentResponseDTO>> getAllShipments() {
        List<Shipment> shipments = supervisorPort.getAllShipments();
        return ResponseEntity.ok(shipmentRestMapper.toResponseList(shipments));
    }

    @GetMapping("/returns")
    public ResponseEntity<List<ReturnRequestResponseDTO>> getAllReturns() {
        List<ReturnRequest> returns = supervisorPort.getAllReturns();
        return ResponseEntity.ok(returnRequestRestMapper.toResponseList(returns));
    }

    @GetMapping("/refunds")
    public ResponseEntity<List<RefundResponseDTO>> getAllRefunds() {
        List<Refund> refunds = supervisorPort.getAllRefunds();
        return ResponseEntity.ok(refundRestMapper.toResponseList(refunds));
    }

    @GetMapping("/reports/consolidated")
    public ResponseEntity<AdministrativeReportResponseDTO> getConsolidatedReport() {
        AdministrativeReport report = supervisorPort.getConsolidatedReport();
        return ResponseEntity.ok(AdministrativeReportResponseDTO.fromDomain(report));
    }

    @GetMapping("/audit/operations")
    public ResponseEntity<List<OperationResponseDTO>> getAuditOperations() {
        List<Operation> operations = supervisorPort.getAuditOperations();
        return ResponseEntity.ok(operationRestMapper.toResponseList(operations));
    }
}

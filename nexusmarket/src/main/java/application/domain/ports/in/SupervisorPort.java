package application.domain.ports.in;

import application.domain.enums.ApprovalDecision;
import application.domain.models.AdministrativeReport;
import application.domain.models.Inventory;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.models.Warehouse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SupervisorPort {
    void reviewDispute(UUID disputeId, ApprovalDecision decision, String notes);
    ReturnRequest approveReturn(UUID returnId, String notes);
    ReturnRequest rejectReturn(UUID returnId, String notes);
    Refund processRefund(UUID returnId, BigDecimal amount, String reason);

    Order getOrderById(UUID orderId);
    List<Order> getAllOrders();
    List<Operation> getAuditOperations();

    List<Warehouse> getAllWarehouses();
    List<Inventory> getAllInventories();
    List<Shipment> getAllShipments();
    List<ReturnRequest> getAllReturns();
    List<Refund> getAllRefunds();
    AdministrativeReport getConsolidatedReport();
}

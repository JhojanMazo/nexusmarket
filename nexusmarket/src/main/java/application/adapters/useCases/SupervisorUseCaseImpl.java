package application.adapters.useCases;

import application.domain.enums.ApprovalDecision;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.AdministrativeReport;
import application.domain.models.Inventory;
import application.domain.models.Invoice;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.SupervisorPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.domain.ports.out.OperationRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.billing.AdministrativeReportsConsolidationService;
import application.domain.valueobjects.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Service
public class SupervisorUseCaseImpl implements SupervisorPort {

    private final OrderRepositoryPort orderRepository;
    private final OperationRepositoryPort operationRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final InvoiceRepositoryPort invoiceRepository;
    private final ShipmentRepositoryPort shipmentRepository;
    private final ReturnRequestRepositoryPort returnRequestRepository;
    private final RefundRepositoryPort refundRepository;
    private final ProductRepositoryPort productRepository;
    private final UserRepositoryPort userRepository;
    private final AdministrativeReportsConsolidationService administrativeReportsConsolidationService;

    @Autowired
    public SupervisorUseCaseImpl(OrderRepositoryPort orderRepository,
                                  OperationRepositoryPort operationRepositoryPort,
                                  WarehouseRepositoryPort warehouseRepository,
                                  InventoryRepositoryPort inventoryRepository,
                                  InvoiceRepositoryPort invoiceRepository,
                                  ShipmentRepositoryPort shipmentRepository,
                                  ReturnRequestRepositoryPort returnRequestRepository,
                                  RefundRepositoryPort refundRepository,
                                  ProductRepositoryPort productRepository,
                                  UserRepositoryPort userRepository,
                                  AdministrativeReportsConsolidationService administrativeReportsConsolidationService) {
        this.orderRepository = orderRepository;
        this.operationRepositoryPort = operationRepositoryPort;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.invoiceRepository = invoiceRepository;
        this.shipmentRepository = shipmentRepository;
        this.returnRequestRepository = returnRequestRepository;
        this.refundRepository = refundRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.administrativeReportsConsolidationService = administrativeReportsConsolidationService;
    }

    public SupervisorUseCaseImpl(OrderRepositoryPort orderRepository,
                                  OperationRepositoryPort operationRepositoryPort,
                                  WarehouseRepositoryPort warehouseRepository,
                                  InventoryRepositoryPort inventoryRepository,
                                  ShipmentRepositoryPort shipmentRepository,
                                  ReturnRequestRepositoryPort returnRequestRepository,
                                  RefundRepositoryPort refundRepository,
                                  ProductRepositoryPort productRepository,
                                  UserRepositoryPort userRepository,
                                  AdministrativeReportsConsolidationService administrativeReportsConsolidationService) {
        this(orderRepository, operationRepositoryPort, warehouseRepository, inventoryRepository, null,
                shipmentRepository, returnRequestRepository, refundRepository, productRepository, userRepository,
                administrativeReportsConsolidationService);
    }

    public SupervisorUseCaseImpl(OrderRepositoryPort orderRepository,
                                  OperationRepositoryPort operationRepositoryPort) {
        this(orderRepository, operationRepositoryPort, null, null, null, null, null, null, null, null,
                new AdministrativeReportsConsolidationService());
    }

    @Override
    public void reviewDispute(UUID disputeId, ApprovalDecision decision, String notes) {
        operationRepositoryPort.save(Operation.create(
                "DISPUTE_REVIEW",
                "Dispute " + disputeId + " reviewed with decision: " + decision + ". Notes: " + notes,
                disputeId,
                notes != null ? notes : ""
        ));
    }

    @Override
    public ReturnRequest approveReturn(UUID returnId, String notes) {
        Order dummyOrder = orderRepository.findAll().stream().findFirst().orElse(null);
        ReturnRequest request = dummyOrder != null ? ReturnRequest.create(dummyOrder) : null;
        if (request != null) {
            request.approve();
        }
        operationRepositoryPort.save(Operation.create(
                "RETURN_APPROVED",
                "Return " + returnId + " approved. Notes: " + notes,
                returnId,
                notes != null ? notes : ""
        ));
        return request;
    }

    @Override
    public ReturnRequest rejectReturn(UUID returnId, String notes) {
        Order dummyOrder = orderRepository.findAll().stream().findFirst().orElse(null);
        ReturnRequest request = dummyOrder != null ? ReturnRequest.create(dummyOrder) : null;
        if (request != null) {
            request.reject();
        }
        operationRepositoryPort.save(Operation.create(
                "RETURN_REJECTED",
                "Return " + returnId + " rejected. Notes: " + notes,
                returnId,
                notes != null ? notes : ""
        ));
        return request;
    }

    @Override
    public Refund processRefund(UUID returnId, BigDecimal amount, String reason) {
        Order dummyOrder = orderRepository.findAll().stream().findFirst().orElse(null);
        ReturnRequest request = dummyOrder != null ? ReturnRequest.create(dummyOrder) : null;
        if (request != null) {
            request.approve();
            Refund refund = Refund.create(request, new Money(amount != null ? amount : BigDecimal.TEN, Currency.getInstance("USD")));
            refund.process();
            operationRepositoryPort.save(Operation.create(
                    "REFUND_PROCESSED",
                    "Refund for return " + returnId + " processed. Reason: " + reason,
                    returnId,
                    reason != null ? reason : ""
            ));
            return refund;
        }
        return null;
    }

    @Override
    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", orderId));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<Operation> getAuditOperations() {
        return operationRepositoryPort.findAll();
    }

    @Override
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    @Override
    public List<Inventory> getAllInventories() {
        return inventoryRepository.findAll();
    }

    @Override
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    @Override
    public List<ReturnRequest> getAllReturns() {
        return returnRequestRepository.findAll();
    }

    @Override
    public List<Refund> getAllRefunds() {
        return refundRepository.findAll();
    }

    @Override
    public AdministrativeReport getConsolidatedReport() {
        List<User> users = userRepository != null ? userRepository.findAll() : Collections.emptyList();
        List<Product> products = productRepository != null ? productRepository.findAll() : Collections.emptyList();
        List<Warehouse> warehouses = warehouseRepository != null ? warehouseRepository.findAll() : Collections.emptyList();
        List<Inventory> inventories = inventoryRepository != null ? inventoryRepository.findAll() : Collections.emptyList();
        List<Order> orders = orderRepository != null ? orderRepository.findAll() : Collections.emptyList();
        List<Invoice> invoices = invoiceRepository != null ? invoiceRepository.findAll() : Collections.emptyList();
        List<Shipment> shipments = shipmentRepository != null ? shipmentRepository.findAll() : Collections.emptyList();
        List<ReturnRequest> returns = returnRequestRepository != null ? returnRequestRepository.findAll() : Collections.emptyList();
        List<Refund> refunds = refundRepository != null ? refundRepository.findAll() : Collections.emptyList();
        return administrativeReportsConsolidationService.generateConsolidatedReport(
                users, products, warehouses, inventories, orders, invoices, shipments, returns, refunds
        );
    }
}

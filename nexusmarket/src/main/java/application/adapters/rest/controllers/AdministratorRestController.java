package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.CreateSellerRequestDTO;
import application.adapters.rest.dtos.requests.CreateStaffUserRequestDTO;
import application.adapters.rest.dtos.requests.CreateWarehouseRequestDTO;
import application.adapters.rest.dtos.requests.GenerateInvoiceRequestDTO;
import application.adapters.rest.dtos.requests.ProcessRefundRequestDTO;
import application.adapters.rest.dtos.requests.UpdateBuyerCommercialStatusRequestDTO;
import application.adapters.rest.dtos.requests.UpdateUserStatusRequestDTO;
import application.adapters.rest.dtos.requests.UpdateWarehouseRequestDTO;
import application.adapters.rest.dtos.responses.AdministrativeReportResponseDTO;
import application.adapters.rest.dtos.responses.BuyerResponseDTO;
import application.adapters.rest.dtos.responses.InvoiceResponseDTO;
import application.adapters.rest.dtos.responses.OperationResponseDTO;
import application.adapters.rest.dtos.responses.RefundResponseDTO;
import application.adapters.rest.dtos.responses.ReturnRequestResponseDTO;
import application.adapters.rest.dtos.responses.SellerResponseDTO;
import application.adapters.rest.dtos.responses.UserResponseDTO;
import application.adapters.rest.dtos.responses.WarehouseResponseDTO;
import application.adapters.rest.mappers.InvoiceRestMapper;
import application.adapters.rest.mappers.OperationRestMapper;
import application.adapters.rest.mappers.OrderRestMapper;
import application.adapters.rest.mappers.RefundRestMapper;
import application.adapters.rest.mappers.ReturnRequestRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.adapters.rest.mappers.WarehouseRestMapper;
import application.domain.enums.WarehouseType;
import application.domain.models.AdministrativeReport;
import application.domain.models.Buyer;
import application.domain.models.Invoice;
import application.domain.models.Operation;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.AdministratorPort;
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
@RequestMapping("/api/admin")
public class AdministratorRestController {

    private final AdministratorPort adminPort;
    private final UserRestMapper userRestMapper;
    private final InvoiceRestMapper invoiceRestMapper;
    private final OperationRestMapper operationRestMapper;
    private final WarehouseRestMapper warehouseRestMapper;
    private final ReturnRequestRestMapper returnRequestRestMapper;
    private final RefundRestMapper refundRestMapper;

    @Autowired
    public AdministratorRestController(AdministratorPort adminPort,
                                        UserRestMapper userRestMapper,
                                        InvoiceRestMapper invoiceRestMapper,
                                        OperationRestMapper operationRestMapper,
                                        WarehouseRestMapper warehouseRestMapper,
                                        ReturnRequestRestMapper returnRequestRestMapper,
                                        RefundRestMapper refundRestMapper) {
        this.adminPort = adminPort;
        this.userRestMapper = userRestMapper;
        this.invoiceRestMapper = invoiceRestMapper;
        this.operationRestMapper = operationRestMapper;
        this.warehouseRestMapper = warehouseRestMapper;
        this.returnRequestRestMapper = returnRequestRestMapper;
        this.refundRestMapper = refundRestMapper;
    }

    public AdministratorRestController(AdministratorPort adminPort,
                                        UserRestMapper userRestMapper,
                                        InvoiceRestMapper invoiceRestMapper,
                                        OperationRestMapper operationRestMapper) {
        this(adminPort, userRestMapper, invoiceRestMapper, operationRestMapper,
                new WarehouseRestMapper(), new ReturnRequestRestMapper(), new RefundRestMapper());
    }

    @PostMapping("/users/staff")
    public ResponseEntity<UserResponseDTO> registerStaff(@RequestBody CreateStaffUserRequestDTO request) {
        User staff = adminPort.registerStaff(
                request.fullName(),
                request.email(),
                request.documentType(),
                request.documentNumber(),
                request.role()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(userRestMapper.toUserResponse(staff));
    }

    @PostMapping("/users/seller")
    public ResponseEntity<SellerResponseDTO> registerSeller(
            @RequestParam(name = "adminUserId", required = false) UUID adminUserId,
            @RequestBody CreateSellerRequestDTO request) {
        UUID effectiveAdmin = adminUserId != null ? adminUserId : UUID.randomUUID();
        Seller seller;
        if (request.warehouseName() != null && !request.warehouseName().isBlank()) {
            seller = adminPort.registerSellerWithWarehouse(
                    effectiveAdmin,
                    request.fullName(),
                    request.email(),
                    request.documentType(),
                    request.documentNumber(),
                    request.onboardingDate(),
                    request.warehouseName(),
                    request.warehouseAddress() != null ? OrderRestMapper.toAddress(request.warehouseAddress()) : null,
                    request.warehouseCapacity()
            );
        } else {
            seller = adminPort.registerSeller(
                    effectiveAdmin,
                    request.fullName(),
                    request.email(),
                    request.documentType(),
                    request.documentNumber(),
                    request.onboardingDate()
            );
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(userRestMapper.toSellerResponse(seller));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> listAllUsers() {
        List<User> users = adminPort.listAllUsers();
        return ResponseEntity.ok(userRestMapper.toUserResponseList(users));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable("userId") UUID userId) {
        User user = adminPort.getUserById(userId);
        return ResponseEntity.ok(userRestMapper.toUserResponse(user));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<UserResponseDTO> updateUserStatus(@PathVariable("userId") UUID userId,
                                                            @RequestBody UpdateUserStatusRequestDTO request) {
        User updated = adminPort.updateUserStatus(userId, request.status());
        return ResponseEntity.ok(userRestMapper.toUserResponse(updated));
    }

    @PatchMapping("/users/buyer/{buyerId}/commercial-status")
    public ResponseEntity<BuyerResponseDTO> updateBuyerCommercialStatus(@PathVariable("buyerId") UUID buyerId,
                                                                        @RequestBody UpdateBuyerCommercialStatusRequestDTO request) {
        Buyer updated = adminPort.updateBuyerCommercialStatus(buyerId, request.commercialStatus());
        return ResponseEntity.ok(userRestMapper.toBuyerResponse(updated));
    }

    @PostMapping("/warehouses")
    public ResponseEntity<WarehouseResponseDTO> createWarehouse(@RequestBody CreateWarehouseRequestDTO request) {
        Warehouse warehouse = adminPort.createWarehouse(
                request.name(),
                request.address() != null ? OrderRestMapper.toAddress(request.address()) : null,
                request.type(),
                request.sellerId(),
                request.capacity()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseRestMapper.toResponse(warehouse));
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponseDTO>> listWarehouses(
            @RequestParam(name = "type", required = false) WarehouseType type,
            @RequestParam(name = "sellerId", required = false) UUID sellerId) {
        List<Warehouse> list;
        if (type != null) {
            list = adminPort.listWarehousesByType(type);
        } else if (sellerId != null) {
            list = adminPort.listWarehousesBySeller(sellerId);
        } else {
            list = adminPort.listAllWarehouses();
        }
        return ResponseEntity.ok(warehouseRestMapper.toResponseList(list));
    }

    @GetMapping("/warehouses/{warehouseId}")
    public ResponseEntity<WarehouseResponseDTO> getWarehouseById(@PathVariable("warehouseId") UUID warehouseId) {
        Warehouse warehouse = adminPort.getWarehouseById(warehouseId);
        return ResponseEntity.ok(warehouseRestMapper.toResponse(warehouse));
    }

    @PatchMapping("/warehouses/{warehouseId}/capacity")
    public ResponseEntity<WarehouseResponseDTO> updateWarehouseCapacity(@PathVariable("warehouseId") UUID warehouseId,
                                                                        @RequestBody UpdateWarehouseRequestDTO request) {
        Warehouse updated = adminPort.updateWarehouseCapacity(warehouseId, request.capacity());
        return ResponseEntity.ok(warehouseRestMapper.toResponse(updated));
    }

    @GetMapping("/returns")
    public ResponseEntity<List<ReturnRequestResponseDTO>> listAllReturns() {
        List<ReturnRequest> returns = adminPort.listAllReturns();
        return ResponseEntity.ok(returnRequestRestMapper.toResponseList(returns));
    }

    @PatchMapping("/returns/{requestId}/approve")
    public ResponseEntity<ReturnRequestResponseDTO> approveReturn(@PathVariable("requestId") UUID requestId) {
        ReturnRequest approved = adminPort.approveReturn(requestId);
        return ResponseEntity.ok(returnRequestRestMapper.toResponse(approved));
    }

    @PatchMapping("/returns/{requestId}/reject")
    public ResponseEntity<ReturnRequestResponseDTO> rejectReturn(@PathVariable("requestId") UUID requestId) {
        ReturnRequest rejected = adminPort.rejectReturn(requestId);
        return ResponseEntity.ok(returnRequestRestMapper.toResponse(rejected));
    }

    @GetMapping("/refunds")
    public ResponseEntity<List<RefundResponseDTO>> listAllRefunds() {
        List<Refund> refunds = adminPort.listAllRefunds();
        return ResponseEntity.ok(refundRestMapper.toResponseList(refunds));
    }

    @PostMapping("/refunds")
    public ResponseEntity<RefundResponseDTO> processRefund(@RequestBody ProcessRefundRequestDTO request) {
        Refund refund = adminPort.processRefund(request.returnRequestId(), request.amount(), request.currencyCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(refundRestMapper.toResponse(refund));
    }

    @GetMapping("/reports/consolidated")
    public ResponseEntity<AdministrativeReportResponseDTO> getAdministrativeReport() {
        AdministrativeReport report = adminPort.getAdministrativeReport();
        return ResponseEntity.ok(AdministrativeReportResponseDTO.fromDomain(report));
    }

    @PostMapping("/invoices")
    public ResponseEntity<InvoiceResponseDTO> generateInvoice(@RequestBody GenerateInvoiceRequestDTO request) {
        Invoice invoice = adminPort.generateInvoice(request.orderId(), request.taxAmount(), request.currencyCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceRestMapper.toResponse(invoice));
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        List<Invoice> invoices = adminPort.getAllInvoices();
        return ResponseEntity.ok(invoiceRestMapper.toResponseList(invoices));
    }

    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable("invoiceId") UUID invoiceId) {
        Invoice invoice = adminPort.getInvoiceById(invoiceId);
        return ResponseEntity.ok(invoiceRestMapper.toResponse(invoice));
    }

    @GetMapping("/invoices/order/{orderId}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceByOrderId(@PathVariable("orderId") UUID orderId) {
        Invoice invoice = adminPort.getInvoiceByOrderId(orderId);
        return ResponseEntity.ok(invoiceRestMapper.toResponse(invoice));
    }

    @PostMapping("/operations")
    public ResponseEntity<OperationResponseDTO> recordOperation(@RequestBody Map<String, String> body) {
        String type = body.getOrDefault("operationType", "ADMIN_OP");
        String desc = body.getOrDefault("description", "Administrative action");
        String operator = body.getOrDefault("operatorId", UUID.randomUUID().toString());
        String payload = body.getOrDefault("payload", "{}");

        Operation operation = adminPort.recordOperation(type, desc, UUID.fromString(operator), payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(operationRestMapper.toResponse(operation));
    }

    @GetMapping("/operations")
    public ResponseEntity<List<OperationResponseDTO>> getAllOperations() {
        List<Operation> operations = adminPort.getAllOperations();
        return ResponseEntity.ok(operationRestMapper.toResponseList(operations));
    }

    @GetMapping("/operations/{operationId}")
    public ResponseEntity<OperationResponseDTO> getOperationById(@PathVariable("operationId") UUID operationId) {
        Operation operation = adminPort.getOperationById(operationId);
        return ResponseEntity.ok(operationRestMapper.toResponse(operation));
    }
}

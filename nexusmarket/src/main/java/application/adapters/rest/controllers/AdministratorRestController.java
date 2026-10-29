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
import application.adapters.rest.dtos.responses.OrderResponseDTO;
import application.adapters.rest.dtos.responses.PageResponseDTO;
import application.adapters.rest.dtos.responses.ProductResponseDTO;
import application.adapters.rest.dtos.responses.RefundResponseDTO;
import application.adapters.rest.dtos.responses.ReturnRequestResponseDTO;
import application.adapters.rest.dtos.responses.SellerResponseDTO;
import application.adapters.rest.dtos.responses.UserResponseDTO;
import application.adapters.rest.dtos.responses.WarehouseResponseDTO;
import application.adapters.rest.mappers.InvoiceRestMapper;
import application.adapters.rest.mappers.OperationRestMapper;
import application.adapters.rest.mappers.OrderRestMapper;
import application.adapters.rest.mappers.ProductRestMapper;
import application.adapters.rest.mappers.RefundRestMapper;
import application.adapters.rest.mappers.ReturnRequestRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.adapters.rest.mappers.WarehouseRestMapper;
import application.domain.enums.WarehouseType;
import application.domain.models.AdministrativeReport;
import application.domain.models.Buyer;
import application.domain.models.Invoice;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.AdministratorPort;
import application.domain.valueobjects.PageResult;
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
    private final OrderRestMapper orderRestMapper;
    private final ProductRestMapper productRestMapper;

    @Autowired
    public AdministratorRestController(AdministratorPort adminPort,
                                        UserRestMapper userRestMapper,
                                        InvoiceRestMapper invoiceRestMapper,
                                        OperationRestMapper operationRestMapper,
                                        WarehouseRestMapper warehouseRestMapper,
                                        ReturnRequestRestMapper returnRequestRestMapper,
                                        RefundRestMapper refundRestMapper,
                                        OrderRestMapper orderRestMapper,
                                        ProductRestMapper productRestMapper) {
        this.adminPort = adminPort;
        this.userRestMapper = userRestMapper;
        this.invoiceRestMapper = invoiceRestMapper;
        this.operationRestMapper = operationRestMapper;
        this.warehouseRestMapper = warehouseRestMapper;
        this.returnRequestRestMapper = returnRequestRestMapper;
        this.refundRestMapper = refundRestMapper;
        this.orderRestMapper = orderRestMapper;
        this.productRestMapper = productRestMapper;
    }

    public AdministratorRestController(AdministratorPort adminPort,
                                        UserRestMapper userRestMapper,
                                        InvoiceRestMapper invoiceRestMapper,
                                        OperationRestMapper operationRestMapper,
                                        WarehouseRestMapper warehouseRestMapper,
                                        ReturnRequestRestMapper returnRequestRestMapper,
                                        RefundRestMapper refundRestMapper) {
        this(adminPort, userRestMapper, invoiceRestMapper, operationRestMapper,
                warehouseRestMapper, returnRequestRestMapper, refundRestMapper,
                new OrderRestMapper(), new ProductRestMapper());
    }

    public AdministratorRestController(AdministratorPort adminPort,
                                        UserRestMapper userRestMapper,
                                        InvoiceRestMapper invoiceRestMapper,
                                        OperationRestMapper operationRestMapper) {
        this(adminPort, userRestMapper, invoiceRestMapper, operationRestMapper,
                new WarehouseRestMapper(), new ReturnRequestRestMapper(), new RefundRestMapper(),
                new OrderRestMapper(), new ProductRestMapper());
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
    public ResponseEntity<PageResponseDTO<UserResponseDTO>> listAllUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        List<User> users = adminPort.listAllUsers();
        return ResponseEntity.ok(PageResponseDTO.of(userRestMapper.toUserResponseList(users), page, size));
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
    public ResponseEntity<PageResponseDTO<WarehouseResponseDTO>> listWarehouses(
            @RequestParam(name = "type", required = false) WarehouseType type,
            @RequestParam(name = "sellerId", required = false) UUID sellerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        List<Warehouse> list;
        if (type != null) {
            list = adminPort.listWarehousesByType(type);
        } else if (sellerId != null) {
            list = adminPort.listWarehousesBySeller(sellerId);
        } else {
            list = adminPort.listAllWarehouses();
        }
        return ResponseEntity.ok(PageResponseDTO.of(warehouseRestMapper.toResponseList(list), page, size));
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

    @GetMapping("/orders")
    public ResponseEntity<PageResponseDTO<OrderResponseDTO>> listAllOrders(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "orderDate") String sortBy,
            @RequestParam(name = "direction", defaultValue = "DESC") String direction) {
        PageResult<Order> ordersPage = adminPort.listAllOrders(page, size, sortBy, direction);
        return ResponseEntity.ok(PageResponseDTO.from(ordersPage, OrderRestMapper::toResponse));
    }

    @GetMapping("/products")
    public ResponseEntity<PageResponseDTO<ProductResponseDTO>> listAllProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(name = "direction", defaultValue = "ASC") String direction) {
        PageResult<Product> productsPage = adminPort.listAllProducts(page, size, sortBy, direction);
        return ResponseEntity.ok(PageResponseDTO.from(productsPage, ProductRestMapper::toResponse));
    }

    @GetMapping("/returns")
    public ResponseEntity<PageResponseDTO<ReturnRequestResponseDTO>> listAllReturns(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        List<ReturnRequest> returns = adminPort.listAllReturns();
        return ResponseEntity.ok(PageResponseDTO.of(returnRequestRestMapper.toResponseList(returns), page, size));
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
    public ResponseEntity<PageResponseDTO<RefundResponseDTO>> listAllRefunds(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        List<Refund> refunds = adminPort.listAllRefunds();
        return ResponseEntity.ok(PageResponseDTO.of(refundRestMapper.toResponseList(refunds), page, size));
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
    public ResponseEntity<PageResponseDTO<InvoiceResponseDTO>> getAllInvoices(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        List<Invoice> invoices = adminPort.getAllInvoices();
        return ResponseEntity.ok(PageResponseDTO.of(invoiceRestMapper.toResponseList(invoices), page, size));
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
    public ResponseEntity<PageResponseDTO<OperationResponseDTO>> getAllOperations(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        List<Operation> operations = adminPort.getAllOperations();
        return ResponseEntity.ok(PageResponseDTO.of(operationRestMapper.toResponseList(operations), page, size));
    }

    @GetMapping("/operations/{operationId}")
    public ResponseEntity<OperationResponseDTO> getOperationById(@PathVariable("operationId") UUID operationId) {
        Operation operation = adminPort.getOperationById(operationId);
        return ResponseEntity.ok(operationRestMapper.toResponse(operation));
    }
}

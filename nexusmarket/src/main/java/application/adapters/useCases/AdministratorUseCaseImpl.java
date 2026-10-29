package application.adapters.useCases;

import application.adapters.aspect.Auditable;
import application.domain.enums.AuditSeverity;
import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.enums.WarehouseType;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.AdministrativeReport;
import application.domain.models.Buyer;
import application.domain.models.Inventory;
import application.domain.models.Invoice;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Seller;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.AdministratorPort;
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
import application.domain.services.billing.RefundsManagementService;
import application.domain.services.inventory.WarehouseInformationControlService;
import application.domain.services.logistics.ReturnsAdministrationService;
import application.domain.services.user.UniqueIdentityValidationService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdministratorUseCaseImpl implements AdministratorPort {

    private final UserRepositoryPort userRepository;
    private final InvoiceRepositoryPort invoiceRepository;
    private final OrderRepositoryPort orderRepository;
    private final ProductRepositoryPort productRepository;
    private final WarehouseRepositoryPort warehouseRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final ShipmentRepositoryPort shipmentRepository;
    private final ReturnRequestRepositoryPort returnRequestRepository;
    private final RefundRepositoryPort refundRepository;
    private final OperationRepositoryPort operationRepositoryPort;
    private final UniqueIdentityValidationService uniqueIdentityValidationService;
    private final WarehouseInformationControlService warehouseInformationControlService;
    private final ReturnsAdministrationService returnsAdministrationService;
    private final RefundsManagementService refundsManagementService;
    private final AdministrativeReportsConsolidationService administrativeReportsConsolidationService;

    @Autowired
    public AdministratorUseCaseImpl(UserRepositoryPort userRepository,
                                    InvoiceRepositoryPort invoiceRepository,
                                    OrderRepositoryPort orderRepository,
                                    ProductRepositoryPort productRepository,
                                    WarehouseRepositoryPort warehouseRepository,
                                    InventoryRepositoryPort inventoryRepository,
                                    ShipmentRepositoryPort shipmentRepository,
                                    ReturnRequestRepositoryPort returnRequestRepository,
                                    RefundRepositoryPort refundRepository,
                                    OperationRepositoryPort operationRepositoryPort,
                                    UniqueIdentityValidationService uniqueIdentityValidationService,
                                    WarehouseInformationControlService warehouseInformationControlService,
                                    ReturnsAdministrationService returnsAdministrationService,
                                    RefundsManagementService refundsManagementService,
                                    AdministrativeReportsConsolidationService administrativeReportsConsolidationService) {
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.shipmentRepository = shipmentRepository;
        this.returnRequestRepository = returnRequestRepository;
        this.refundRepository = refundRepository;
        this.operationRepositoryPort = operationRepositoryPort;
        this.uniqueIdentityValidationService = uniqueIdentityValidationService;
        this.warehouseInformationControlService = warehouseInformationControlService;
        this.returnsAdministrationService = returnsAdministrationService;
        this.refundsManagementService = refundsManagementService;
        this.administrativeReportsConsolidationService = administrativeReportsConsolidationService;
    }

    public AdministratorUseCaseImpl(UserRepositoryPort userRepository,
                                    InvoiceRepositoryPort invoiceRepository,
                                    OrderRepositoryPort orderRepository,
                                    ProductRepositoryPort productRepository,
                                    WarehouseRepositoryPort warehouseRepository,
                                    ShipmentRepositoryPort shipmentRepository,
                                    ReturnRequestRepositoryPort returnRequestRepository,
                                    RefundRepositoryPort refundRepository,
                                    OperationRepositoryPort operationRepositoryPort,
                                    UniqueIdentityValidationService uniqueIdentityValidationService,
                                    WarehouseInformationControlService warehouseInformationControlService,
                                    ReturnsAdministrationService returnsAdministrationService,
                                    RefundsManagementService refundsManagementService,
                                    AdministrativeReportsConsolidationService administrativeReportsConsolidationService) {
        this(userRepository, invoiceRepository, orderRepository, productRepository, warehouseRepository, null,
                shipmentRepository, returnRequestRepository, refundRepository, operationRepositoryPort,
                uniqueIdentityValidationService, warehouseInformationControlService, returnsAdministrationService,
                refundsManagementService, administrativeReportsConsolidationService);
    }

    public AdministratorUseCaseImpl(UserRepositoryPort userRepository,
                                    InvoiceRepositoryPort invoiceRepository,
                                    OrderRepositoryPort orderRepository,
                                    OperationRepositoryPort operationRepositoryPort,
                                    UniqueIdentityValidationService uniqueIdentityValidationService) {
        this(userRepository, invoiceRepository, orderRepository, null, null, null, null, null, null,
                operationRepositoryPort, uniqueIdentityValidationService,
                new WarehouseInformationControlService(), new ReturnsAdministrationService(),
                new RefundsManagementService(), new AdministrativeReportsConsolidationService());
    }

    @Override
    @Transactional
    @Auditable(action = "STAFF_REGISTERED", resource = "User", severity = AuditSeverity.HIGH)
    public User registerStaff(String fullName, String email, String docType, String docNumber, UserRole role) {
        uniqueIdentityValidationService.validateUniqueIdentity(userRepository, email, docType, docNumber);

        User staff = User.createStaff(
                fullName,
                new Email(email),
                new IdentityDocument(docType, docNumber),
                role
        );
        return userRepository.save(staff);
    }

    @Override
    @Transactional
    public Seller registerSeller(UUID adminUserId, String fullName, String email, String docType, String docNumber, LocalDate onboardingDate) {
        uniqueIdentityValidationService.validateUniqueIdentity(userRepository, email, docType, docNumber);

        Seller seller = Seller.create(
                adminUserId,
                fullName,
                new Email(email),
                new IdentityDocument(docType, docNumber),
                onboardingDate != null ? onboardingDate : LocalDate.now()
        );
        User saved = userRepository.save(seller);
        return (Seller) saved;
    }

    @Override
    @Transactional
    public Seller registerSellerWithWarehouse(UUID adminUserId, String fullName, String email, String docType, String docNumber,
                                               LocalDate onboardingDate, String warehouseName, Address warehouseAddress, Double capacity) {
        Seller seller = registerSeller(adminUserId, fullName, email, docType, docNumber, onboardingDate);
        if (warehouseName != null && !warehouseName.isBlank()) {
            Address address = warehouseAddress != null ? warehouseAddress : new Address("Default Street", "City", "State", "Country", "00000", null);
            double cap = capacity != null ? capacity : 1000.0;
            Warehouse warehouse = new Warehouse(UUID.randomUUID(), warehouseName, address, WarehouseType.SELLER, seller.getUserId(), cap);
            warehouseRepository.save(warehouse);
        }
        return seller;
    }

    @Override
    @Transactional
    @Auditable(action = "USER_STATUS_UPDATED", resource = "User", severity = AuditSeverity.CRITICAL)
    public User updateUserStatus(UUID userId, UserStatus newStatus) {
        User user = getUserById(userId);
        user.setStatus(newStatus);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public Buyer updateBuyerCommercialStatus(UUID buyerId, BuyerCommercialStatus status) {
        User user = getUserById(buyerId);
        if (!(user instanceof Buyer buyer)) {
            throw new IllegalArgumentException("El usuario no es un comprador (BUYER)");
        }
        buyer.setCommercialStatus(status);
        return (Buyer) userRepository.save(buyer);
    }

    @Override
    @Transactional
    public Warehouse createWarehouse(String name, Address address, WarehouseType type, UUID sellerId, double capacity) {
        if (type == WarehouseType.SELLER && sellerId != null) {
            userRepository.findById(sellerId).orElseThrow(() -> new ResourceNotFoundException("Vendedor", sellerId));
        }
        Warehouse warehouse = new Warehouse(UUID.randomUUID(), name, address, type, sellerId, capacity);
        return warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional
    public Warehouse updateWarehouseCapacity(UUID warehouseId, double capacity) {
        Warehouse warehouse = getWarehouseById(warehouseId);
        warehouseInformationControlService.updateWarehouseCapacity(warehouse, capacity);
        return warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public Warehouse getWarehouseById(UUID warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Bodega", warehouseId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Warehouse> listAllWarehouses() {
        return warehouseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Warehouse> listWarehousesByType(WarehouseType type) {
        return warehouseRepository.findByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Warehouse> listWarehousesBySeller(UUID sellerId) {
        return warehouseRepository.findBySellerId(sellerId);
    }

    @Override
    @Transactional
    @Auditable(action = "RETURN_APPROVED", resource = "ReturnRequest", severity = AuditSeverity.HIGH)
    public ReturnRequest approveReturn(UUID returnRequestId) {
        ReturnRequest request = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de devolución", returnRequestId));
        returnsAdministrationService.approveReturn(request);
        return returnRequestRepository.save(request);
    }

    @Override
    @Transactional
    @Auditable(action = "RETURN_REJECTED", resource = "ReturnRequest", severity = AuditSeverity.HIGH)
    public ReturnRequest rejectReturn(UUID returnRequestId) {
        ReturnRequest request = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de devolución", returnRequestId));
        returnsAdministrationService.rejectReturn(request);
        return returnRequestRepository.save(request);
    }

    @Override
    @Transactional
    @Auditable(action = "REFUND_PROCESSED", resource = "Refund", severity = AuditSeverity.CRITICAL)
    public Refund processRefund(UUID returnRequestId, BigDecimal amount, String currencyCode) {
        ReturnRequest request = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de devolución", returnRequestId));
        Currency currency = currencyCode != null ? Currency.getInstance(currencyCode) : Currency.getInstance("USD");
        Refund refund = refundsManagementService.processRefund(request, new Money(amount, currency));
        returnsAdministrationService.completeReturn(request);
        returnRequestRepository.save(request);
        return refundRepository.save(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> listAllReturns() {
        return returnRequestRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Refund> listAllRefunds() {
        return refundRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Order> listAllOrders(int page, int size, String sortBy, String direction) {
        if (orderRepository == null) {
            return PageResult.of(List.of(), page, size);
        }
        return orderRepository.findAll(page, size, sortBy, direction);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Product> listAllProducts(int page, int size, String sortBy, String direction) {
        if (productRepository == null) {
            return PageResult.of(List.of(), page, size);
        }
        return productRepository.findAll(page, size, sortBy, direction);
    }

    @Override
    @Transactional(readOnly = true)
    public AdministrativeReport getAdministrativeReport() {
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

    @Override
    @Transactional
    @Auditable(action = "INVOICE_GENERATED", resource = "Invoice", severity = AuditSeverity.HIGH)
    public Invoice generateInvoice(UUID orderId, BigDecimal taxAmount, String currencyCode) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", orderId));

        if (order.getStatus() == OrderStatus.CART) {
            throw new BusinessRuleException("No se puede facturar una orden que aún está en estado CARRITO");
        }

        var existing = invoiceRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Currency currency;
        if (currencyCode != null && !currencyCode.isBlank()) {
            currency = Currency.getInstance(currencyCode.trim().toUpperCase(Locale.ROOT));
        } else {
            currency = order.subtotal().currency();
        }

        BigDecimal taxVal = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        Money tax = new Money(taxVal, currency);

        Invoice invoice = new Invoice(
                UUID.randomUUID(),
                order,
                LocalDate.now(),
                order.subtotal(),
                tax
        );

        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoiceById(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", invoiceId));
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoiceByOrderId(UUID orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura para la orden", orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    @Override
    @Transactional
    public Operation recordOperation(String operationType, String description, UUID operatorId, String payload) {
        Operation operation = Operation.create(operationType, description, operatorId, payload);
        return operationRepositoryPort.save(operation);
    }

    @Override
    @Transactional(readOnly = true)
    public Operation getOperationById(UUID operationId) {
        return operationRepositoryPort.findById(operationId)
                .orElseThrow(() -> new EntityNotFoundException("Operación", operationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operation> getAllOperations() {
        return operationRepositoryPort.findAll();
    }
}

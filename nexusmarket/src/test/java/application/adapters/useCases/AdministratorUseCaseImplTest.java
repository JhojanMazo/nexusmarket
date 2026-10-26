package application.adapters.useCases;

import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.enums.WarehouseType;
import application.domain.models.AdministrativeReport;
import application.domain.models.Buyer;
import application.domain.models.Inventory;
import application.domain.models.Invoice;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.models.ShoppingCart;
import application.domain.models.User;
import application.domain.models.Warehouse;
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
import application.domain.valueobjects.OrderItem;
import application.domain.valueobjects.ProductVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministratorUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private InvoiceRepositoryPort invoiceRepository;
    @Mock
    private OrderRepositoryPort orderRepository;
    @Mock
    private ProductRepositoryPort productRepository;
    @Mock
    private WarehouseRepositoryPort warehouseRepository;
    @Mock
    private InventoryRepositoryPort inventoryRepository;
    @Mock
    private ShipmentRepositoryPort shipmentRepository;
    @Mock
    private ReturnRequestRepositoryPort returnRequestRepository;
    @Mock
    private RefundRepositoryPort refundRepository;
    @Mock
    private OperationRepositoryPort operationRepositoryPort;
    @Mock
    private UniqueIdentityValidationService uniqueIdentityValidationService;

    private AdministratorUseCaseImpl adminUseCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adminUseCase = new AdministratorUseCaseImpl(
                userRepository,
                invoiceRepository,
                orderRepository,
                productRepository,
                warehouseRepository,
                inventoryRepository,
                shipmentRepository,
                returnRequestRepository,
                refundRepository,
                operationRepositoryPort,
                uniqueIdentityValidationService,
                new WarehouseInformationControlService(),
                new ReturnsAdministrationService(),
                new RefundsManagementService(),
                new AdministrativeReportsConsolidationService()
        );
    }

    @Test
    void registerStaff_Success() {
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User staff = adminUseCase.registerStaff(
                "Super Admin",
                "admin@nexus.com",
                "CC",
                "12345",
                UserRole.ADMINISTRATOR
        );

        assertNotNull(staff);
        assertEquals("Super Admin", staff.getFullName());
        assertEquals(UserRole.ADMINISTRATOR, staff.getRole());
    }

    @Test
    void updateUserStatus_Success() {
        User user = User.createStaff("User Staff", new Email("staff@nexus.com"),
                new IdentityDocument("CC", "123"), UserRole.LOGISTICS_OPERATOR);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = adminUseCase.updateUserStatus(userId, UserStatus.BLOCKED);
        assertEquals(UserStatus.BLOCKED, updated.getStatus());
    }

    @Test
    void generateInvoice_Success() {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        ShoppingCart cart = new ShoppingCart(UUID.randomUUID(), buyer);
        Product product = new Product(UUID.randomUUID(), "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        cart.addItem(new OrderItem(product, 1, new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);
        order.advanceTo(OrderStatus.PENDING_PAYMENT);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(invoiceRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        Invoice invoice = adminUseCase.generateInvoice(orderId, BigDecimal.valueOf(190), "USD");
        assertNotNull(invoice);
        assertEquals(BigDecimal.valueOf(1190), invoice.getTotal().amount());
    }

    @Test
    void recordOperation_Success() {
        when(operationRepositoryPort.save(any(Operation.class))).thenAnswer(i -> i.getArgument(0));

        Operation operation = adminUseCase.recordOperation(
                "SYS_AUDIT",
                "Audit routine run",
                userId,
                "{}"
        );

        assertNotNull(operation);
        assertEquals("SYS_AUDIT", operation.getOperationType());
    }

    @Test
    void getAdministrativeReport_ConsolidatesSalesInvoicesOrdersInventoryReturnsAndRefunds() {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        Product product = new Product(UUID.randomUUID(), "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(500), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        Warehouse wh = new Warehouse(UUID.randomUUID(), "Central WH", address, WarehouseType.MARKETPLACE, null, 1000.0);
        Inventory inv = Inventory.create(product, wh, 20);
        inv.reserve(5);

        ShoppingCart cart = new ShoppingCart(UUID.randomUUID(), buyer);
        cart.addItem(new OrderItem(product, 2, new Money(BigDecimal.valueOf(500), Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);
        order.advanceTo(OrderStatus.PENDING_PAYMENT);
        order.advanceTo(OrderStatus.PAID);
        order.advanceTo(OrderStatus.SHIPPED);
        order.advanceTo(OrderStatus.DELIVERED_COMPLETED);

        Invoice invoice = new Invoice(UUID.randomUUID(), order, LocalDate.now(), order.subtotal(),
                new Money(BigDecimal.valueOf(100), Currency.getInstance("USD")));
        ReturnRequest returnRequest = ReturnRequest.create(order);
        returnRequest.approve();
        Refund refund = Refund.create(returnRequest, new Money(BigDecimal.valueOf(500), Currency.getInstance("USD")));
        refund.process();

        when(userRepository.findAll()).thenReturn(List.of(buyer));
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(warehouseRepository.findAll()).thenReturn(List.of(wh));
        when(inventoryRepository.findAll()).thenReturn(List.of(inv));
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice));
        when(shipmentRepository.findAll()).thenReturn(List.of());
        when(returnRequestRepository.findAll()).thenReturn(List.of(returnRequest));
        when(refundRepository.findAll()).thenReturn(List.of(refund));

        AdministrativeReport report = adminUseCase.getAdministrativeReport();
        assertEquals(1L, report.totalOrders());
        assertEquals(1L, report.ordersByStatus().get("DELIVERED_COMPLETED"));
        assertEquals(BigDecimal.valueOf(1000), report.totalRevenue());
        assertEquals(1L, report.totalInvoices());
        assertEquals(BigDecimal.valueOf(1100), report.totalInvoicedAmount());
        assertEquals(15L, report.totalInventoryAvailableUnits());
        assertEquals(5L, report.totalInventoryReservedUnits());
        assertEquals(1L, report.inventoryLotsByStatus().get("AVAILABLE"));
        assertEquals(1L, report.totalReturns());
        assertEquals(1L, report.returnsByStatus().get("APPROVED"));
        assertEquals(1L, report.totalRefunds());
        assertEquals(1L, report.refundsByStatus().get("PROCESSED"));
        assertEquals(BigDecimal.valueOf(500), report.totalRefundedAmount());
    }
}

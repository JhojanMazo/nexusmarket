package application.adapters.rest;

import application.adapters.rest.controllers.AdministratorRestController;
import application.adapters.rest.exception.GlobalExceptionHandler;
import application.adapters.rest.mappers.InvoiceRestMapper;
import application.adapters.rest.mappers.OperationRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.models.Buyer;
import application.domain.models.Invoice;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ShoppingCart;
import application.domain.models.User;
import application.domain.ports.in.AdministratorPort;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.domain.enums.WarehouseType;
import application.domain.models.AdministrativeReport;
import application.domain.models.Warehouse;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class AdministratorRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdministratorPort adminPort;

    private final UUID userId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        AdministratorRestController controller = new AdministratorRestController(
                adminPort,
                new UserRestMapper(),
                new InvoiceRestMapper(),
                new OperationRestMapper()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registerStaff_Success() throws Exception {
        User staff = User.createStaff("Supervisor One", new Email("sup@nexus.com"),
                new IdentityDocument("CC", "777"), UserRole.SUPERVISOR);

        when(adminPort.registerStaff(eq("Supervisor One"), eq("sup@nexus.com"), eq("CC"), eq("777"), eq(UserRole.SUPERVISOR)))
                .thenReturn(staff);

        String json = """
                {
                    "fullName": "Supervisor One",
                    "email": "sup@nexus.com",
                    "documentType": "CC",
                    "documentNumber": "777",
                    "role": "SUPERVISOR"
                }
                """;

        mockMvc.perform(post("/api/admin/users/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Supervisor One"))
                .andExpect(jsonPath("$.role").value("SUPERVISOR"));
    }

    @Test
    void updateUserStatus_Success() throws Exception {
        User user = User.createStaff("User Staff", new Email("staff@nexus.com"),
                new IdentityDocument("CC", "123"), UserRole.LOGISTICS_OPERATOR);
        user.setStatus(UserStatus.BLOCKED);

        when(adminPort.updateUserStatus(eq(userId), eq(UserStatus.BLOCKED))).thenReturn(user);

        String json = """
                {
                    "status": "BLOCKED"
                }
                """;

        mockMvc.perform(patch("/api/admin/users/{userId}/status", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void generateInvoice_Success() throws Exception {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        ShoppingCart cart = new ShoppingCart(UUID.randomUUID(), buyer);
        Product product = new Product(UUID.randomUUID(), "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        cart.addItem(new OrderItem(product, 1, new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);
        order.advanceTo(OrderStatus.PENDING_PAYMENT);

        Invoice invoice = new Invoice(UUID.randomUUID(), order, LocalDate.now(),
                order.subtotal(), new Money(BigDecimal.valueOf(190), Currency.getInstance("USD")));

        when(adminPort.generateInvoice(eq(orderId), any(BigDecimal.class), eq("USD"))).thenReturn(invoice);

        String json = """
                {
                    "orderId": "%s",
                    "taxAmount": 190,
                    "currency": "USD"
                }
                """.formatted(orderId);

        mockMvc.perform(post("/api/admin/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(1190));
    }

    @Test
    void recordOperation_Success() throws Exception {
        UUID opId = UUID.randomUUID();
        Operation operation = new Operation(opId, "TAX_AUDIT", "Audit complete", userId, "SUCCESS", "{}", java.time.Instant.now());

        when(adminPort.recordOperation(eq("TAX_AUDIT"), any(), any(), any())).thenReturn(operation);

        String json = """
                {
                    "operationType": "TAX_AUDIT",
                    "description": "Audit complete",
                    "operatorId": "%s",
                    "payload": "{}"
                }
                """.formatted(userId);

        mockMvc.perform(post("/api/admin/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.operationType").value("TAX_AUDIT"));
    }

    @Test
    void createWarehouse_Success() throws Exception {
        Address address = new Address("Cra 15", "Bogota", "DC", "Colombia", "110111", null);
        Warehouse warehouse = new Warehouse(UUID.randomUUID(), "Bodega Principal", address, WarehouseType.MARKETPLACE, null, 5000.0);
        when(adminPort.createWarehouse(eq("Bodega Principal"), any(Address.class), eq(WarehouseType.MARKETPLACE), any(), eq(5000.0)))
                .thenReturn(warehouse);

        String json = """
                {
                    "name": "Bodega Principal",
                    "address": {
                        "street": "Cra 15",
                        "city": "Bogota",
                        "stateOrProvince": "DC",
                        "country": "Colombia",
                        "postalCode": "110111"
                    },
                    "type": "MARKETPLACE",
                    "capacity": 5000.0
                }
                """;

        mockMvc.perform(post("/api/admin/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Bodega Principal"))
                .andExpect(jsonPath("$.capacity").value(5000.0));
    }

    @Test
    void getAdministrativeReport_Success() throws Exception {
        AdministrativeReport report = new AdministrativeReport(
                10L, Map.of("BUYER", 5L, "SELLER", 3L, "ADMINISTRATOR", 2L),
                20L, 2L, 15L, Map.of("PAID", 10L, "DELIVERED_COMPLETED", 5L),
                new BigDecimal("15000.00"), 5L, 1L, 1L
        );
        when(adminPort.getAdministrativeReport()).thenReturn(report);

        mockMvc.perform(get("/api/admin/reports/consolidated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalProducts").value(20))
                .andExpect(jsonPath("$.totalRevenue").value(15000.00));
    }
}

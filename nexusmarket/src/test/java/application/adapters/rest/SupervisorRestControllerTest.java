package application.adapters.rest;

import application.adapters.rest.controllers.SupervisorRestController;
import application.adapters.rest.exception.GlobalExceptionHandler;
import application.adapters.rest.mappers.OperationRestMapper;
import application.adapters.rest.mappers.OrderRestMapper;
import application.domain.enums.ApprovalDecision;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ReturnRequest;
import application.domain.models.ShoppingCart;
import application.domain.ports.in.SupervisorPort;
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
import java.util.Currency;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SupervisorRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SupervisorPort supervisorPort;

    private final UUID disputeId = UUID.randomUUID();
    private final UUID returnId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        SupervisorRestController controller = new SupervisorRestController(
                supervisorPort,
                new OrderRestMapper(),
                new OperationRestMapper()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void reviewDispute_Success() throws Exception {
        doNothing().when(supervisorPort).reviewDispute(eq(disputeId), eq(ApprovalDecision.APPROVED), any());

        mockMvc.perform(post("/api/supervisors/disputes/{disputeId}/review", disputeId)
                        .param("decision", "APPROVED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\": \"Valid claim\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("APPROVED"));
    }

    @Test
    void approveReturn_Success() throws Exception {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        ShoppingCart cart = new ShoppingCart(UUID.randomUUID(), buyer);
        Product product = new Product(UUID.randomUUID(), "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        cart.addItem(new OrderItem(product, 1, new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);
        ReturnRequest request = ReturnRequest.create(order);
        request.approve();

        when(supervisorPort.approveReturn(eq(returnId), any())).thenReturn(request);

        mockMvc.perform(post("/api/supervisors/returns/{returnId}/approve", returnId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\": \"Approved return\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getOrderById_Success() throws Exception {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        ShoppingCart cart = new ShoppingCart(UUID.randomUUID(), buyer);
        Product product = new Product(UUID.randomUUID(), "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        cart.addItem(new OrderItem(product, 1, new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);

        when(supervisorPort.getOrderById(orderId)).thenReturn(order);

        mockMvc.perform(get("/api/supervisors/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CART"));
    }
}

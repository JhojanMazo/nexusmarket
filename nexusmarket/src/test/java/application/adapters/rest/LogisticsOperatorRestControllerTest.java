package application.adapters.rest;

import application.adapters.rest.controllers.LogisticsOperatorRestController;
import application.adapters.rest.exception.GlobalExceptionHandler;
import application.adapters.rest.mappers.OrderRestMapper;
import application.domain.enums.InventoryMovementType;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.models.Buyer;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Shipment;
import application.domain.models.ShoppingCart;
import application.domain.models.User;
import application.domain.ports.in.LogisticsOperatorPort;
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
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LogisticsOperatorRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LogisticsOperatorPort logisticsPort;

    private final UUID orderId = UUID.randomUUID();
    private final UUID inventoryId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        LogisticsOperatorRestController controller = new LogisticsOperatorRestController(
                logisticsPort,
                new OrderRestMapper()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void updateDeliveryStatus_Success() throws Exception {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        ShoppingCart cart = new ShoppingCart(UUID.randomUUID(), buyer);
        Product product = new Product(UUID.randomUUID(), "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        cart.addItem(new OrderItem(product, 1, new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);
        order.advanceTo(OrderStatus.PENDING_PAYMENT);
        order.advanceTo(OrderStatus.PAID);
        order.advanceTo(OrderStatus.SHIPPED);

        when(logisticsPort.updateDeliveryStatus(eq(orderId), eq(OrderStatus.SHIPPED))).thenReturn(order);

        String json = """
                {
                    "status": "SHIPPED"
                }
                """;

        mockMvc.perform(patch("/api/logistics/orders/{orderId}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void registerMovement_Success() throws Exception {
        InventoryMovement movement = new InventoryMovement(
                UUID.randomUUID(),
                inventoryId,
                InventoryMovementType.INBOUND,
                10,
                Instant.now(),
                "Stock receipt"
        );

        when(logisticsPort.registerMovement(eq(inventoryId), eq(10), eq("INBOUND"), any()))
                .thenReturn(movement);

        String json = """
                {
                    "quantity": 10,
                    "type": "INBOUND",
                    "reason": "Stock receipt"
                }
                """;

        mockMvc.perform(post("/api/logistics/inventory/{inventoryId}/movements", inventoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.type").value("INBOUND"));
    }

    @Test
    void reserveStock_Success() throws Exception {
        doNothing().when(logisticsPort).reserveInventory(eq(productId), eq(5));

        mockMvc.perform(post("/api/logistics/inventory/reserve")
                        .param("productId", productId.toString())
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andExpect(jsonPath("$.reservedQuantity").value(5));
    }

    @Test
    void dispatchOrder_Success() throws Exception {
        UUID operatorId = UUID.randomUUID();
        User operator = User.createStaff("Log Operator", new Email("log@test.com"), new IdentityDocument("CC", "1234"), UserRole.LOGISTICS_OPERATOR);
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        ShoppingCart cart = new ShoppingCart(UUID.randomUUID(), buyer);
        Product product = new Product(UUID.randomUUID(), "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        cart.addItem(new OrderItem(product, 1, new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);
        order.advanceTo(OrderStatus.PENDING_PAYMENT);
        order.advanceTo(OrderStatus.PAID);

        Shipment shipment = Shipment.create(operator, order);
        shipment.registerDispatch();

        when(logisticsPort.dispatchOrder(eq(orderId), eq(operatorId))).thenReturn(shipment);

        String json = """
                {
                    "orderId": "%s",
                    "logisticsOperatorId": "%s"
                }
                """.formatted(orderId, operatorId);

        mockMvc.perform(post("/api/logistics/shipments/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DISPATCHED"));
    }
}

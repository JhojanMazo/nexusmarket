package application.adapters.rest;

import application.adapters.rest.controllers.BuyerRestController;
import application.adapters.rest.exception.GlobalExceptionHandler;
import application.adapters.rest.mappers.CartRestMapper;
import application.adapters.rest.mappers.OrderRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ShoppingCart;
import application.domain.ports.in.BuyerPort;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BuyerRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BuyerPort buyerPort;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        BuyerRestController controller = new BuyerRestController(
                buyerPort,
                new UserRestMapper(),
                new CartRestMapper(),
                new OrderRestMapper()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createCart_Success() throws Exception {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        ShoppingCart cart = new ShoppingCart(cartId, buyer);

        when(buyerPort.createCart(buyerId)).thenReturn(cart);

        mockMvc.perform(post("/api/buyers/{buyerId}/cart", buyerId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartId").value(cartId.toString()));
    }

    @Test
    void getCartByBuyerId_Success() throws Exception {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        ShoppingCart cart = new ShoppingCart(cartId, buyer);

        when(buyerPort.getCartByBuyerId(buyerId)).thenReturn(cart);

        mockMvc.perform(get("/api/buyers/{buyerId}/cart", buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(cartId.toString()));
    }

    @Test
    void createOrder_Success() throws Exception {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        ShoppingCart cart = new ShoppingCart(cartId, buyer);
        Product product = new Product(UUID.randomUUID(), "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        cart.addItem(new OrderItem(product, 1, new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);

        when(buyerPort.createOrderFromCart(eq(cartId), any(Address.class))).thenReturn(order);

        String json = """
                {
                    "cartId": "%s",
                    "deliveryAddress": {
                        "street": "Cra 1",
                        "city": "Bogota",
                        "stateOrProvince": "DC",
                        "country": "Colombia",
                        "postalCode": "1101"
                    }
                }
                """.formatted(cartId);

        mockMvc.perform(post("/api/buyers/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(1000));
    }

    @Test
    void payOrder_Success() throws Exception {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        ShoppingCart cart = new ShoppingCart(cartId, buyer);
        Product product = new Product(UUID.randomUUID(), "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        cart.addItem(new OrderItem(product, 1, new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);
        order.advanceTo(OrderStatus.PENDING_PAYMENT);
        order.advanceTo(OrderStatus.PAID);

        when(buyerPort.processPayment(eq(orderId), any(), any())).thenReturn(order);

        String json = """
                {
                    "orderId": "%s",
                    "amount": 1000,
                    "currency": "USD",
                    "paymentMethod": "CREDIT_CARD"
                }
                """.formatted(orderId);

        mockMvc.perform(post("/api/buyers/orders/{orderId}/pay", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void manageSecondaryAddresses_AddUpdateDelete_Success() throws Exception {
        Address primary = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Address sec1 = new Address("Calle 50", "Medellin", "Antioquia", "Colombia", "0500", "Apt 101");
        Buyer buyerWithSec = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"),
                primary, List.of(sec1), BuyerCommercialStatus.ENABLED);

        when(buyerPort.addAdditionalAddress(eq(buyerId), eq(buyerId), any(Address.class))).thenReturn(buyerWithSec);
        when(buyerPort.updateAdditionalAddress(eq(buyerId), eq(buyerId), eq(0), any(Address.class))).thenReturn(buyerWithSec);
        Buyer buyerAfterDelete = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), primary);
        when(buyerPort.removeAdditionalAddress(eq(buyerId), eq(buyerId), eq(0))).thenReturn(buyerAfterDelete);

        String addrJson = """
                {
                    "street": "Calle 50",
                    "city": "Medellin",
                    "stateOrProvince": "Antioquia",
                    "country": "Colombia",
                    "postalCode": "0500",
                    "reference": "Apt 101"
                }
                """;

        mockMvc.perform(post("/api/buyers/{buyerId}/addresses", buyerId)
                        .param("authenticatedBuyerId", buyerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addrJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.additionalAddresses[0].city").value("Medellin"));

        mockMvc.perform(put("/api/buyers/{buyerId}/addresses/0", buyerId)
                        .param("authenticatedBuyerId", buyerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addrJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.additionalAddresses[0].city").value("Medellin"));

        mockMvc.perform(delete("/api/buyers/{buyerId}/addresses/0", buyerId)
                        .param("authenticatedBuyerId", buyerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.additionalAddresses").isEmpty());
    }
}

package application.adapters.useCases;

import application.domain.enums.InventoryMovementType;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Buyer;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ShoppingCart;
import application.domain.ports.out.OrderRepositoryPort;
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
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogisticsOperatorUseCaseImplTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    private LogisticsOperatorUseCaseImpl logisticsUseCase;

    private final UUID orderId = UUID.randomUUID();
    private final UUID inventoryId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        logisticsUseCase = new LogisticsOperatorUseCaseImpl(orderRepository);
    }

    @Test
    void updateDeliveryStatus_Success() {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        ShoppingCart cart = new ShoppingCart(UUID.randomUUID(), buyer);
        Product product = new Product(UUID.randomUUID(), "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        cart.addItem(new OrderItem(product, 1, new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);
        order.advanceTo(OrderStatus.PENDING_PAYMENT);
        order.advanceTo(OrderStatus.PAID);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order updated = logisticsUseCase.updateDeliveryStatus(orderId, OrderStatus.SHIPPED);
        assertEquals(OrderStatus.SHIPPED, updated.getStatus());
    }

    @Test
    void registerMovement_Success() {
        InventoryMovement movement = logisticsUseCase.registerMovement(
                inventoryId,
                25,
                "INBOUND",
                "Replenishment"
        );

        assertNotNull(movement);
        assertEquals(25, movement.quantity());
        assertEquals(InventoryMovementType.INBOUND, movement.type());
    }
}

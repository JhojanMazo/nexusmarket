package application.adapters.useCases;

import application.domain.enums.ApprovalDecision;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ReturnRequest;
import application.domain.models.ShoppingCart;
import application.domain.ports.out.OperationRepositoryPort;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupervisorUseCaseImplTest {

    @Mock
    private OrderRepositoryPort orderRepository;
    @Mock
    private OperationRepositoryPort operationRepositoryPort;

    private SupervisorUseCaseImpl supervisorUseCase;

    private final UUID disputeId = UUID.randomUUID();
    private final UUID returnId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        supervisorUseCase = new SupervisorUseCaseImpl(orderRepository, operationRepositoryPort);
    }

    @Test
    void reviewDispute_Success() {
        assertDoesNotThrow(() ->
                supervisorUseCase.reviewDispute(disputeId, ApprovalDecision.APPROVED, "Resolved amicably")
        );
    }

    @Test
    void approveReturn_Success() {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        ShoppingCart cart = new ShoppingCart(UUID.randomUUID(), buyer);
        Product product = new Product(UUID.randomUUID(), "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        cart.addItem(new OrderItem(product, 1, new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);

        when(orderRepository.findAll()).thenReturn(List.of(order));

        ReturnRequest approved = supervisorUseCase.approveReturn(returnId, "Return valid");
        assertNotNull(approved);
        assertEquals(ReturnRequest.ReturnRequestStatus.APPROVED, approved.getStatus());
    }
}

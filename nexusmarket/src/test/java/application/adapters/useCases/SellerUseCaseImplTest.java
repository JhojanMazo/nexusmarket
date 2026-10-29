package application.adapters.useCases;

import application.adapters.security.SecurityUserContext;
import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.ShoppingCart;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.user.UniqueIdentityValidationService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import application.domain.valueobjects.ProductVariant;
import org.junit.jupiter.api.AfterEach;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private ProductRepositoryPort productRepository;
    @Mock
    private OrderRepositoryPort orderRepository;
    @Mock
    private UniqueIdentityValidationService uniqueIdentityValidationService;

    private SellerUseCaseImpl sellerUseCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        sellerUseCase = new SellerUseCaseImpl(
                userRepository,
                productRepository,
                orderRepository,
                uniqueIdentityValidationService
        );
    }

    @Test
    void registerSeller_Success() {
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        Seller seller = sellerUseCase.registerSeller(
                UUID.randomUUID(),
                "Seller Corp",
                "seller@nexus.com",
                "NIT",
                "900123",
                LocalDate.now()
        );

        assertNotNull(seller);
        assertEquals("Seller Corp", seller.getFullName());
    }

    @Test
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product product = sellerUseCase.createProduct(
                "Mechanical Keyboard",
                "Switch",
                "Cherry MX Red",
                BigDecimal.valueOf(120),
                "USD",
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );

        assertNotNull(product);
        assertEquals("Mechanical Keyboard", product.getName());
        assertEquals(ProductStatus.PUBLISHED, product.getStatus());
    }

    @Test
    void updateProductPrice_Success() {
        Product existing = new Product(productId, "Keyboard", new ProductVariant("Switch", "Blue"),
                new Money(BigDecimal.valueOf(100), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product updated = sellerUseCase.updateProductPrice(productId, BigDecimal.valueOf(115), "USD");
        assertEquals(BigDecimal.valueOf(115), updated.getPrice().amount());
    }

    @AfterEach
    void tearDown() {
        SecurityUserContext.clear();
    }

    @Test
    void updateProductPrice_OtherSeller_ThrowsUnauthorizedOperationException() {
        UUID ownerSellerId = UUID.randomUUID();
        UUID otherSellerId = UUID.randomUUID();
        Product product = new Product(productId, ownerSellerId, "Keyboard", new ProductVariant("Switch", "Blue"),
                new Money(BigDecimal.valueOf(100), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(UnauthorizedOperationException.class, () ->
                sellerUseCase.updateProductPrice(otherSellerId, productId, BigDecimal.valueOf(150), "USD"));
    }

    @Test
    void updateProductStatus_OtherSeller_ThrowsUnauthorizedOperationException() {
        UUID ownerSellerId = UUID.randomUUID();
        UUID otherSellerId = UUID.randomUUID();
        Product product = new Product(productId, ownerSellerId, "Keyboard", new ProductVariant("Switch", "Blue"),
                new Money(BigDecimal.valueOf(100), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(UnauthorizedOperationException.class, () ->
                sellerUseCase.updateProductStatus(otherSellerId, productId, ProductStatus.SUSPENDED));
    }

    @Test
    void getSellerOrders_FiltersOnlyProductsOwnedBySeller() {
        UUID sellerA = UUID.randomUUID();
        UUID sellerB = UUID.randomUUID();

        Product productA = new Product(UUID.randomUUID(), sellerA, "Prod A", new ProductVariant("V", "1"),
                new Money(BigDecimal.valueOf(50), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        Product productB = new Product(UUID.randomUUID(), sellerB, "Prod B", new ProductVariant("V", "2"),
                new Money(BigDecimal.valueOf(80), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);

        Address addr = new Address("Main St", "City", "State", "Country", "12345", null);
        Buyer buyer = Buyer.create("Buyer One", new Email("b@test.com"), new IdentityDocument("CC", "123"), addr);

        ShoppingCart cart1 = new ShoppingCart(UUID.randomUUID(), buyer);
        cart1.addItem(new OrderItem(productA, 1, productA.getPrice()));
        Order orderSellerA = Order.createFromCart(cart1, addr);

        ShoppingCart cart2 = new ShoppingCart(UUID.randomUUID(), buyer);
        cart2.addItem(new OrderItem(productB, 2, productB.getPrice()));
        Order orderSellerB = Order.createFromCart(cart2, addr);

        when(orderRepository.findAll()).thenReturn(List.of(orderSellerA, orderSellerB));

        List<Order> ordersForA = sellerUseCase.getSellerOrders(sellerA);
        assertEquals(1, ordersForA.size());
        assertEquals(orderSellerA.getOrderId(), ordersForA.get(0).getOrderId());

        List<Order> ordersForB = sellerUseCase.getSellerOrders(sellerB);
        assertEquals(1, ordersForB.size());
        assertEquals(orderSellerB.getOrderId(), ordersForB.get(0).getOrderId());
    }

    @Test
    void antiSpoofing_DifferentAuthenticatedSeller_ThrowsUnauthorizedOperationException() {
        UUID authenticatedSellerId = UUID.randomUUID();
        UUID spoofedSellerId = UUID.randomUUID();

        SecurityUserContext.set(new AuthenticatedUserPrincipal(authenticatedSellerId, "seller@nexus.com", "SELLER"));

        assertThrows(UnauthorizedOperationException.class, () ->
                sellerUseCase.getSellerOrders(spoofedSellerId));
    }
}

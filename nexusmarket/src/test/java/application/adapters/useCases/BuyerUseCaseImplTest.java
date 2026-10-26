package application.adapters.useCases;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.InventoryStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.WarehouseType;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ShoppingCart;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.ports.out.ShoppingCartRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.inventory.CriticalStockValidationService;
import application.domain.services.inventory.InventoryReservationService;
import application.domain.services.logistics.ReturnsAdministrationService;
import application.domain.services.product.ProductTypeDifferentiationService;
import application.domain.services.user.PrimaryAddressManagementService;
import application.domain.services.user.SecondaryAddressManagementService;
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
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuyerUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private ShoppingCartRepositoryPort cartRepository;
    @Mock
    private ProductRepositoryPort productRepository;
    @Mock
    private OrderRepositoryPort orderRepository;
    @Mock
    private InventoryRepositoryPort inventoryRepository;
    @Mock
    private ReturnRequestRepositoryPort returnRequestRepository;
    @Mock
    private UniqueIdentityValidationService uniqueIdentityValidationService;

    private BuyerUseCaseImpl buyerUseCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private Buyer buyer;
    private Address address;

    @BeforeEach
    void setUp() {
        buyerUseCase = new BuyerUseCaseImpl(
                userRepository,
                cartRepository,
                productRepository,
                orderRepository,
                inventoryRepository,
                returnRequestRepository,
                uniqueIdentityValidationService,
                new ReturnsAdministrationService(),
                new InventoryReservationService(),
                new CriticalStockValidationService(),
                new ProductTypeDifferentiationService(),
                new ValidateBuyerOwnershipService(),
                new PrimaryAddressManagementService(),
                new SecondaryAddressManagementService()
        );
        address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        buyer = Buyer.create("Buyer Test", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
    }

    @Test
    void createCart_Success() {
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(cartRepository.save(any(ShoppingCart.class))).thenAnswer(i -> i.getArgument(0));

        ShoppingCart cart = buyerUseCase.createCart(buyerId);
        assertNotNull(cart);
        assertEquals(buyer, cart.getBuyer());
    }

    @Test
    void addItemToCart_Success() {
        ShoppingCart cart = new ShoppingCart(cartId, buyer);
        Product product = new Product(productId, "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(ShoppingCart.class))).thenAnswer(i -> i.getArgument(0));

        ShoppingCart updated = buyerUseCase.addItem(cartId, productId, 1, BigDecimal.valueOf(1000), "USD");
        assertEquals(1, updated.getItems().size());
    }

    @Test
    void createOrderFromCart_Success() {
        ShoppingCart cart = new ShoppingCart(cartId, buyer);
        Product product = new Product(productId, "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        cart.addItem(new OrderItem(product, 1, new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD"))));

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order order = buyerUseCase.createOrderFromCart(cartId, address);
        assertNotNull(order);
        verify(cartRepository).deleteById(cartId);
    }

    @Test
    void processPayment_Success() {
        ShoppingCart cart = new ShoppingCart(cartId, buyer);
        Product product = new Product(productId, "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        cart.addItem(new OrderItem(product, 1, new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);

        Warehouse wh = new Warehouse(UUID.randomUUID(), "WH", address, WarehouseType.MARKETPLACE, null, 100.0);
        Inventory inv = Inventory.create(product, wh, 5);

        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductId(productId)).thenReturn(List.of(inv));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order paidOrder = buyerUseCase.processPayment(order.getOrderId(), BigDecimal.valueOf(1000), "CREDIT_CARD");
        assertEquals(OrderStatus.PAID, paidOrder.getStatus());
        assertEquals(4, inv.getAvailableQuantity());
        assertEquals(1, inv.getReservedQuantity());
    }

    @Test
    void processPayment_DigitalOnlyOrder_TransitionsDirectlyToDeliveredCompleted() {
        ShoppingCart cart = new ShoppingCart(cartId, buyer);
        Product digitalProduct = new Product(productId, "Software License", new ProductVariant("Edition", "Pro"),
                new Money(BigDecimal.valueOf(200), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.DIGITAL);
        cart.addItem(new OrderItem(digitalProduct, 1, new Money(BigDecimal.valueOf(200), Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);

        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order completedOrder = buyerUseCase.processPayment(order.getOrderId(), BigDecimal.valueOf(200), "CREDIT_CARD");
        assertEquals(OrderStatus.DELIVERED_COMPLETED, completedOrder.getStatus());
    }

    @Test
    void processPayment_PhysicalOrder_DamagedOrInsufficientStock_ThrowsIllegalStateException() {
        ShoppingCart cart = new ShoppingCart(cartId, buyer);
        Product product = new Product(productId, "Laptop", new ProductVariant("RAM", "16GB"),
                new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        cart.addItem(new OrderItem(product, 3, new Money(BigDecimal.valueOf(1000), Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);

        Warehouse wh = new Warehouse(UUID.randomUUID(), "WH", address, WarehouseType.MARKETPLACE, null, 100.0);
        Inventory damagedInv = Inventory.create(product, wh, 10);
        damagedInv.setStatus(InventoryStatus.DAMAGED);

        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductId(productId)).thenReturn(List.of(damagedInv));

        assertThrows(IllegalStateException.class,
                () -> buyerUseCase.processPayment(order.getOrderId(), BigDecimal.valueOf(3000), "CREDIT_CARD"));
    }

    @Test
    void manageSecondaryAddresses_WithOwnershipValidation_SuccessAndUnauthorized() {
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(userRepository.save(any(Buyer.class))).thenAnswer(i -> i.getArgument(0));

        Address sec1 = new Address("Calle 50", "Medellin", "Antioquia", "Colombia", "0500", "Apt 101");
        Address sec2 = new Address("Calle 80", "Cali", "Valle", "Colombia", "7600", "Oficina 202");

        Buyer afterAdd = buyerUseCase.addAdditionalAddress(buyerId, buyerId, sec1);
        assertEquals(1, afterAdd.getSecondaryAddresses().size());
        assertEquals("Medellin", afterAdd.getSecondaryAddresses().get(0).city());

        Buyer afterUpdate = buyerUseCase.updateAdditionalAddress(buyerId, buyerId, 0, sec2);
        assertEquals(1, afterUpdate.getSecondaryAddresses().size());
        assertEquals("Cali", afterUpdate.getSecondaryAddresses().get(0).city());

        Buyer afterRemove = buyerUseCase.removeAdditionalAddress(buyerId, buyerId, 0);
        assertEquals(0, afterRemove.getSecondaryAddresses().size());

        UUID intruderId = UUID.randomUUID();
        assertThrows(UnauthorizedOperationException.class,
                () -> buyerUseCase.addAdditionalAddress(intruderId, buyerId, sec1));
    }
}

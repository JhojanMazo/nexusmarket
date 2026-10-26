package application.adapters.persistence.sql;
import application.adapters.persistence.jpa.*;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.valueobjects.*;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderJpaAdapterTest {

    @Mock
    private SpringDataJpaOrderRepository jpaRepository;

    @Mock
    private SpringDataJpaProductRepository productJpaRepository;

    private OrderJpaMapper mapper;
    private OrderJpaAdapter adapter;

    private Order order;
    private OrderJpaEntity orderEntity;
    private UUID orderId;
    private UUID buyerId;
    private Product product;

    @BeforeEach
    void setUp() {
        UserJpaMapper userMapper = new UserJpaMapper();
        ProductJpaMapper productMapper = new ProductJpaMapper();
        mapper = new OrderJpaMapper(userMapper, productMapper);
        adapter = new OrderJpaAdapter(jpaRepository, productJpaRepository, mapper);

        buyerId = UUID.randomUUID();
        Buyer buyer = Buyer.reconstitute(
                buyerId,
                "Paula Andrea",
                new Email("paula@example.com"),
                new IdentityDocument("CC", "43210987"),
                new Address("Calle 20", "Cali", "Valle", "Colombia", "760001", null),
                List.of(),
                BuyerCommercialStatus.ENABLED,
                application.domain.enums.UserStatus.ACTIVE
        );

        product = new Product(
                UUID.randomUUID(),
                "Teclado Gamer",
                new ProductVariant("Switch", "Red"),
                new Money(new BigDecimal("90.00"), Currency.getInstance("USD")),
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );

        orderId = UUID.randomUUID();
        order = Order.reconstitute(
                orderId,
                buyer,
                List.of(new OrderItem(product, 1, new Money(new BigDecimal("90.00"), Currency.getInstance("USD")))),
                buyer.getPrimaryAddress(),
                OrderStatus.PENDING_PAYMENT,
                LocalDate.now()
        );

        ProductJpaEntity pEntity = productMapper.toEntity(product);
        when(productJpaRepository.findById(product.getProductId())).thenReturn(Optional.of(pEntity));
        orderEntity = mapper.toEntity(order, productJpaRepository);
    }

    @Test
    void save_Success() {
        when(jpaRepository.save(any(OrderJpaEntity.class))).thenReturn(orderEntity);

        Order saved = adapter.save(order);

        assertNotNull(saved);
        assertEquals(orderId, saved.getOrderId());
        assertEquals(OrderStatus.PENDING_PAYMENT, saved.getStatus());
        assertEquals(1, saved.getItems().size());
        verify(jpaRepository, times(1)).save(any(OrderJpaEntity.class));
    }

    @Test
    void findById_Success() {
        when(jpaRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        Optional<Order> found = adapter.findById(orderId);

        assertTrue(found.isPresent());
        assertEquals(orderId, found.get().getOrderId());
        assertEquals(OrderStatus.PENDING_PAYMENT, found.get().getStatus());
    }

    @Test
    void findByBuyerId_Success() {
        when(jpaRepository.findByBuyer_UserId(buyerId)).thenReturn(List.of(orderEntity));

        List<Order> found = adapter.findByBuyerId(buyerId);

        assertEquals(1, found.size());
        assertEquals(orderId, found.get(0).getOrderId());
    }
}

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
import application.adapters.persistence.jpa.entities.OrderItemJpaEntity;
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
import org.mockito.ArgumentCaptor;
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
        lenient().when(productJpaRepository.findById(product.getProductId())).thenReturn(Optional.of(pEntity));
        orderEntity = mapper.toJpaEntity(order, p -> pEntity);
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
    void save_newOrder_mapsEveryItemAndLinksItToTheOrder() {
        when(jpaRepository.findById(orderId)).thenReturn(Optional.empty());
        when(jpaRepository.save(any(OrderJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(order);

        ArgumentCaptor<OrderJpaEntity> captor = ArgumentCaptor.forClass(OrderJpaEntity.class);
        verify(jpaRepository).save(captor.capture());
        OrderJpaEntity persisted = captor.getValue();
        assertEquals(1, persisted.getItems().size());
        OrderItemJpaEntity item = persisted.getItems().get(0);
        assertSame(persisted, item.getOrder(), "Item must point back to its order (order_id FK)");
        assertEquals(product.getProductId(), item.getProduct().getProductId());
        assertEquals(1, item.getQuantity());
        assertEquals(0, new BigDecimal("90.00").compareTo(item.getUnitPriceAmount()));
        assertEquals("USD", item.getCurrencyCode());
    }

    @Test
    void save_existingOrder_updatesManagedEntityAndKeepsItemRows() {
        OrderItemJpaEntity existingRow = orderEntity.getItems().get(0);
        existingRow.setOrderItemId(UUID.randomUUID());
        when(jpaRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(jpaRepository.save(any(OrderJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        order.advanceTo(OrderStatus.PAID);
        Order saved = adapter.save(order);

        verify(jpaRepository).save(same(orderEntity));
        assertEquals(OrderStatus.PAID, orderEntity.getStatus());
        assertEquals(1, orderEntity.getItems().size());
        assertSame(existingRow, orderEntity.getItems().get(0), "Unchanged items must not be recreated");
        assertEquals(OrderStatus.PAID, saved.getStatus());
    }

    @Test
    void save_existingOrder_removesItemsNoLongerInTheAggregate() {
        Product mouse = new Product(UUID.randomUUID(), "Mouse", new ProductVariant("DPI", "1600"),
                new Money(new BigDecimal("20.00"), Currency.getInstance("USD")),
                ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        ProductJpaEntity mouseEntity = new ProductJpaMapper().toEntity(mouse);
        OrderItemJpaEntity staleRow = new OrderItemJpaEntity(mouseEntity, 2, new BigDecimal("20.0000"), "USD");
        orderEntity.addItem(staleRow);
        when(jpaRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(jpaRepository.save(any(OrderJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(order);

        assertEquals(1, orderEntity.getItems().size());
        assertFalse(orderEntity.getItems().contains(staleRow));
        assertNull(staleRow.getOrder(), "Removed item must be detached from the order (orphan)");
    }

    @Test
    void save_withUnknownProduct_throwsAndDoesNotPersist() {
        when(productJpaRepository.findById(product.getProductId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> adapter.save(order));
        verify(jpaRepository, never()).save(any());
    }

    @Test
    void findById_Success() {
        when(jpaRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        Optional<Order> found = adapter.findById(orderId);

        assertTrue(found.isPresent());
        assertEquals(orderId, found.get().getOrderId());
        assertEquals(OrderStatus.PENDING_PAYMENT, found.get().getStatus());
        assertEquals(1, found.get().getItems().size());
        OrderItem item = found.get().getItems().get(0);
        assertEquals(product.getProductId(), item.product().getProductId());
        assertEquals(1, item.quantity());
        assertEquals(0, new BigDecimal("90.00").compareTo(item.subtotal().amount()));
    }

    @Test
    void findByBuyerId_Success() {
        when(jpaRepository.findByBuyer_UserId(buyerId)).thenReturn(List.of(orderEntity));

        List<Order> found = adapter.findByBuyerId(buyerId);

        assertEquals(1, found.size());
        assertEquals(orderId, found.get(0).getOrderId());
    }
}

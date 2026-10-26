package application.adapters.persistence.sql;

import application.adapters.persistence.jpa.ShipmentJpaAdapter;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import application.adapters.persistence.jpa.entities.UserEntity;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.mappers.ShipmentJpaMapper;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaShipmentRepository;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Shipment;
import application.domain.models.ShoppingCart;
import application.domain.models.User;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentJpaAdapterTest {

    @Mock
    private SpringDataJpaShipmentRepository jpaRepository;

    @Mock
    private SpringDataJpaOrderRepository orderRepository;

    @Mock
    private SpringDataJpaProductRepository productRepository;

    private ShipmentJpaMapper mapper;
    private ShipmentJpaAdapter adapter;

    private Shipment shipment;
    private ShipmentJpaEntity entity;
    private UUID shipmentId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        UserJpaMapper userJpaMapper = new UserJpaMapper();
        ProductJpaMapper productJpaMapper = new ProductJpaMapper();
        OrderJpaMapper orderJpaMapper = new OrderJpaMapper(userJpaMapper, productJpaMapper);
        mapper = new ShipmentJpaMapper(orderJpaMapper, userJpaMapper);
        adapter = new ShipmentJpaAdapter(jpaRepository, orderRepository, productRepository, mapper);

        Address address = new Address("Street", "City", "State", "Country", "11111", null);
        Buyer buyer = Buyer.create("John Buyer", new Email("buyer@test.com"), new IdentityDocument("CC", "1234"), address);
        Product product = new Product(UUID.randomUUID(), "Book", new ProductVariant("Edition", "1"),
                new Money(BigDecimal.TEN, Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);

        ShoppingCart cart = new ShoppingCart(UUID.randomUUID(), buyer);
        cart.addItem(new OrderItem(product, 2, new Money(BigDecimal.TEN, Currency.getInstance("USD"))));
        Order order = Order.createFromCart(cart, address);
        orderId = order.getOrderId();

        User operator = User.createStaff("Logistics John", new Email("log@test.com"), new IdentityDocument("CC", "5555"), UserRole.LOGISTICS_OPERATOR);
        shipment = Shipment.create(operator, order);
        shipmentId = shipment.getShipmentId();

        entity = mapper.toEntity(shipment, orderRepository, productRepository);
    }

    @Test
    void save_Success() {
        when(jpaRepository.save(any(ShipmentJpaEntity.class))).thenReturn(entity);

        Shipment saved = adapter.save(shipment);

        assertNotNull(saved);
        assertEquals(shipmentId, saved.getShipmentId());
        assertEquals(Shipment.ShipmentStatus.CREATED, saved.getStatus());
        verify(jpaRepository, times(1)).save(any(ShipmentJpaEntity.class));
    }

    @Test
    void findById_Success() {
        when(jpaRepository.findById(shipmentId)).thenReturn(Optional.of(entity));

        Optional<Shipment> found = adapter.findById(shipmentId);

        assertTrue(found.isPresent());
        assertEquals(shipmentId, found.get().getShipmentId());
    }

    @Test
    void findByOrderId_Success() {
        when(jpaRepository.findByOrderOrderId(orderId)).thenReturn(Optional.of(entity));

        Optional<Shipment> found = adapter.findByOrderId(orderId);

        assertTrue(found.isPresent());
        assertEquals(orderId, found.get().getOrder().getOrderId());
    }
}

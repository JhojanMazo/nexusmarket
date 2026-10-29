package application.adapters.persistence.sql;

import application.adapters.persistence.jpa.OrderJpaAdapter;
import application.adapters.persistence.jpa.entities.BuyerEntity;
import application.adapters.persistence.jpa.entities.OrderItemJpaEntity;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserStatus;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import application.domain.valueobjects.ProductVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test of {@link OrderJpaAdapter} against a real (H2) relational database.
 * Verifies that the {@code order_items} table is written, read and cleaned up through the
 * {@code orders → order_items} cascade, not just kept in memory.
 *
 * <p>Uses the pooled (Hikari) DataSource instead of the default non-pooled embedded one:
 * H2 2.4.x binds {@code IN (...)} check constraints (e.g. the {@code users.role} discriminator)
 * to the DDL session, so closing that connection breaks later inserts.</p>
 */
@DataJpaTest(properties = "spring.datasource.url=jdbc:h2:mem:order_items_it;DB_CLOSE_DELAY=-1")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({OrderJpaAdapter.class, OrderJpaMapper.class, UserJpaMapper.class, ProductJpaMapper.class})
class OrderJpaAdapterIntegrationTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Autowired
    private OrderJpaAdapter adapter;

    @Autowired
    private SpringDataJpaOrderRepository orderJpaRepository;

    @Autowired
    private TestEntityManager em;

    private Buyer buyer;
    private Product keyboard;
    private Product mouse;

    @BeforeEach
    void setUp() {
        UserJpaMapper userMapper = new UserJpaMapper();
        ProductJpaMapper productMapper = new ProductJpaMapper();

        buyer = Buyer.reconstitute(
                UUID.randomUUID(),
                "Paula Andrea",
                new Email("paula@example.com"),
                new IdentityDocument("CC", "43210987"),
                new Address("Calle 20", "Cali", "Valle", "Colombia", "760001", null),
                List.of(),
                BuyerCommercialStatus.ENABLED,
                UserStatus.ACTIVE
        );
        keyboard = new Product(UUID.randomUUID(), "Teclado Gamer", new ProductVariant("Switch", "Red"),
                money("90.00"), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        mouse = new Product(UUID.randomUUID(), "Mouse", new ProductVariant("DPI", "16000"),
                money("25.50"), ProductStatus.PUBLISHED, ProductType.PHYSICAL);

        em.persist((BuyerEntity) userMapper.toEntity(buyer));
        em.persist(productMapper.toEntity(keyboard));
        em.persist(productMapper.toEntity(mouse));
        em.flush();
        em.clear();
    }

    @Test
    void save_newOrder_persistsEveryItemInOrderItemsTable() {
        Order order = newOrder(List.of(
                new OrderItem(keyboard, 2, money("90.00")),
                new OrderItem(mouse, 1, money("25.50"))));

        adapter.save(order);
        flushAndClear();

        assertEquals(2L, countOrderItemRows(order.getOrderId()));
        OrderJpaEntity stored = em.find(OrderJpaEntity.class, order.getOrderId());
        assertTrue(stored.getItems().stream().allMatch(i -> i.getOrder() == stored),
                "Every item must reference its owning order (bidirectional FK order_id)");
        assertTrue(stored.getItems().stream().allMatch(i -> i.getOrderItemId() != null));
    }

    @Test
    void findById_reloadsItemsFromDatabaseWithoutLosingFields() {
        Order order = newOrder(List.of(
                new OrderItem(keyboard, 2, money("90.00")),
                new OrderItem(mouse, 3, money("25.50"))));
        adapter.save(order);
        flushAndClear();

        Order reloaded = adapter.findById(order.getOrderId()).orElseThrow();

        assertEquals(2, reloaded.getItems().size());
        Map<UUID, OrderItem> byProduct = reloaded.getItems().stream()
                .collect(Collectors.toMap(i -> i.product().getProductId(), Function.identity()));

        OrderItem k = byProduct.get(keyboard.getProductId());
        assertEquals("Teclado Gamer", k.product().getName());
        assertEquals(ProductType.PHYSICAL, k.product().getType());
        assertEquals(2, k.quantity());
        assertEquals(0, new BigDecimal("90.00").compareTo(k.unitPrice().amount()));
        assertEquals(USD, k.unitPrice().currency());
        assertEquals(0, new BigDecimal("180.00").compareTo(k.subtotal().amount()));

        OrderItem m = byProduct.get(mouse.getProductId());
        assertEquals(3, m.quantity());
        assertEquals(0, new BigDecimal("76.50").compareTo(m.subtotal().amount()));

        assertEquals(0, new BigDecimal("256.50").compareTo(reloaded.total().amount()));
    }

    @Test
    void save_existingOrder_updatesStatusWithoutRecreatingItemRows() {
        Order order = newOrder(List.of(
                new OrderItem(keyboard, 2, money("90.00")),
                new OrderItem(mouse, 1, money("25.50"))));
        adapter.save(order);
        flushAndClear();
        Set<UUID> idsBefore = itemIds(order.getOrderId());

        Order loaded = adapter.findById(order.getOrderId()).orElseThrow();
        loaded.advanceTo(OrderStatus.PAID);
        adapter.save(loaded);
        flushAndClear();

        assertEquals(OrderStatus.PAID, em.find(OrderJpaEntity.class, order.getOrderId()).getStatus());
        assertEquals(2L, countOrderItemRows(order.getOrderId()));
        assertEquals(idsBefore, itemIds(order.getOrderId()),
                "Updating an order must keep the same order_items rows (no delete/re-insert churn)");
    }

    @Test
    void save_orderWithItemsRemoved_deletesOrphanRows() {
        Order order = newOrder(List.of(
                new OrderItem(keyboard, 2, money("90.00")),
                new OrderItem(mouse, 1, money("25.50"))));
        adapter.save(order);
        flushAndClear();

        Order withOneItem = Order.reconstitute(order.getOrderId(), buyer,
                List.of(new OrderItem(keyboard, 2, money("90.00"))),
                order.getDeliveryAddress(), order.getStatus(), order.getCreationDate());
        adapter.save(withOneItem);
        flushAndClear();

        assertEquals(1L, countOrderItemRows(order.getOrderId()));
        Order reloaded = adapter.findById(order.getOrderId()).orElseThrow();
        assertEquals(1, reloaded.getItems().size());
        assertEquals(keyboard.getProductId(), reloaded.getItems().get(0).product().getProductId());
    }

    @Test
    void removeItemHelper_triggersOrphanRemoval() {
        Order order = newOrder(List.of(
                new OrderItem(keyboard, 2, money("90.00")),
                new OrderItem(mouse, 1, money("25.50"))));
        adapter.save(order);
        flushAndClear();

        OrderJpaEntity managed = em.find(OrderJpaEntity.class, order.getOrderId());
        OrderItemJpaEntity toRemove = managed.getItems().get(0);
        managed.removeItem(toRemove);
        flushAndClear();

        assertEquals(1L, countOrderItemRows(order.getOrderId()));
    }

    @Test
    void deletingOrder_cascadesDeleteToOrderItems() {
        Order order = newOrder(List.of(
                new OrderItem(keyboard, 2, money("90.00")),
                new OrderItem(mouse, 1, money("25.50"))));
        adapter.save(order);
        flushAndClear();
        assertEquals(2L, countOrderItemRows(order.getOrderId()));

        orderJpaRepository.deleteById(order.getOrderId());
        flushAndClear();

        assertEquals(0L, countAllOrderItemRows());
    }

    @Test
    void findByBuyerId_returnsOrdersWithTheirPersistedItems() {
        adapter.save(newOrder(List.of(new OrderItem(keyboard, 1, money("90.00")))));
        adapter.save(newOrder(List.of(
                new OrderItem(keyboard, 1, money("90.00")),
                new OrderItem(mouse, 4, money("25.50")))));
        flushAndClear();

        List<Order> orders = adapter.findByBuyerId(buyer.getUserId());

        assertEquals(2, orders.size());
        assertEquals(Set.of(1, 2), orders.stream().map(o -> o.getItems().size()).collect(Collectors.toSet()));
        assertEquals(3L, countAllOrderItemRows());
    }

    @Test
    void save_withUnknownProduct_failsFastAndPersistsNothing() {
        Product ghost = new Product(UUID.randomUUID(), "Ghost", new ProductVariant("X", "Y"),
                money("1.00"), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        Order order = newOrder(List.of(new OrderItem(ghost, 1, money("1.00"))));

        assertThrows(IllegalStateException.class, () -> adapter.save(order));
        assertEquals(0L, countAllOrderItemRows());
    }

    // ---------------------------------------------------------------- helpers

    private Order newOrder(List<OrderItem> items) {
        return Order.reconstitute(UUID.randomUUID(), buyer, items, buyer.getPrimaryAddress(),
                OrderStatus.PENDING_PAYMENT, LocalDate.now());
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount), USD);
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private long countOrderItemRows(UUID orderId) {
        return em.getEntityManager()
                .createQuery("select count(i) from OrderItemJpaEntity i where i.order.orderId = :id", Long.class)
                .setParameter("id", orderId)
                .getSingleResult();
    }

    private long countAllOrderItemRows() {
        Number n = (Number) em.getEntityManager()
                .createNativeQuery("SELECT COUNT(*) FROM order_items")
                .getSingleResult();
        return n.longValue();
    }

    private Set<UUID> itemIds(UUID orderId) {
        return em.getEntityManager()
                .createQuery("select i.orderItemId from OrderItemJpaEntity i where i.order.orderId = :id", UUID.class)
                .setParameter("id", orderId)
                .getResultList()
                .stream()
                .collect(Collectors.toSet());
    }
}

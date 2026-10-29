package application.adapters.persistence.sql;

import application.adapters.config.JpaAuditingConfig;
import application.adapters.persistence.jpa.InventoryJpaAdapter;
import application.adapters.persistence.jpa.OrderJpaAdapter;
import application.adapters.persistence.jpa.ProductJpaAdapter;
import application.adapters.persistence.jpa.entities.BuyerEntity;
import application.adapters.persistence.jpa.entities.InventoryJpaEntity;
import application.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import application.adapters.persistence.jpa.mappers.InventoryJpaMapper;
import application.adapters.persistence.jpa.mappers.InventoryMovementJpaMapper;
import application.adapters.persistence.jpa.mappers.InvoiceJpaMapper;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.mappers.WarehouseJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInventoryRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserStatus;
import application.domain.enums.WarehouseType;
import application.domain.models.Buyer;
import application.domain.models.Inventory;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import application.domain.valueobjects.ProductVariant;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests verifying enterprise concurrency control ({@code @Version} optimistic locking)
 * and JPA Auditing ({@code createdAt}, {@code updatedAt}) across the main relational entities.
 */
@DataJpaTest(properties = "spring.datasource.url=jdbc:h2:mem:concurrency_audit_it;DB_CLOSE_DELAY=-1")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaAuditingConfig.class,
        ProductJpaAdapter.class,
        ProductJpaMapper.class,
        InventoryJpaAdapter.class,
        InventoryJpaMapper.class,
        InventoryMovementJpaMapper.class,
        WarehouseJpaMapper.class,
        OrderJpaAdapter.class,
        OrderJpaMapper.class,
        UserJpaMapper.class,
        InvoiceJpaMapper.class
})
class ConcurrencyAndAuditingIntegrationTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Autowired
    private ProductJpaAdapter productAdapter;

    @Autowired
    private InventoryJpaAdapter inventoryAdapter;

    @Autowired
    private OrderJpaAdapter orderAdapter;

    @Autowired
    private SpringDataJpaProductRepository productRepository;

    @Autowired
    private SpringDataJpaInventoryRepository inventoryRepository;

    @Autowired
    private UserJpaMapper userMapper;

    @Autowired
    private WarehouseJpaMapper warehouseMapper;

    @Autowired
    private InvoiceJpaMapper invoiceMapper;

    @Autowired
    private TestEntityManager em;

    @Test
    void productOptimisticLocking_incrementsVersionAndDetectsConcurrentModification() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(
                productId,
                "Monitor 4K",
                new ProductVariant("Size", "27in"),
                new Money(new BigDecimal("350.00"), USD),
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );

        productAdapter.save(product);
        em.flush();
        em.clear();

        ProductJpaEntity persisted = productRepository.findById(productId).orElseThrow();
        assertEquals(0L, persisted.getVersion());
        assertNotNull(persisted.getCreatedAt());
        assertNotNull(persisted.getUpdatedAt());

        // Simulate a stale detached copy held by a concurrent transaction (version = 0)
        ProductJpaEntity staleCopy = new ProductJpaEntity();
        staleCopy.setProductId(productId);
        staleCopy.setName("Stale Name");
        staleCopy.setVariantAttribute("Size");
        staleCopy.setVariantValue("27in");
        staleCopy.setPriceAmount(new BigDecimal("300.00"));
        staleCopy.setCurrencyCode("USD");
        staleCopy.setStatus(ProductStatus.PUBLISHED);
        staleCopy.setType(ProductType.PHYSICAL);
        staleCopy.setVersion(0L);

        // First update succeeds and increments version to 1
        Product updatedProduct = product.withPrice(new Money(new BigDecimal("399.00"), USD));
        productAdapter.save(updatedProduct);
        em.flush();
        em.clear();

        ProductJpaEntity afterFirstUpdate = productRepository.findById(productId).orElseThrow();
        assertEquals(1L, afterFirstUpdate.getVersion());

        // Second concurrent update with stale version (0) must fail with OptimisticLockException
        assertThrows(OptimisticLockException.class, () -> {
            em.getEntityManager().merge(staleCopy);
            em.flush();
        });
    }

    @Test
    void inventoryOptimisticLocking_incrementsVersionAndDetectsConcurrentModification() {
        Product product = new Product(
                UUID.randomUUID(),
                "SSD NVMe 1TB",
                new ProductVariant("Capacity", "1TB"),
                new Money(new BigDecimal("120.00"), USD),
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );
        productAdapter.save(product);

        Address address = new Address("Av. Industrial 10", "Medellin", "Antioquia", "Colombia", "050001", null);
        Warehouse warehouse = new Warehouse(
                UUID.randomUUID(), "Bodega Central", address, WarehouseType.MARKETPLACE, null, 1000.0
        );
        WarehouseJpaEntity warehouseEntity = warehouseMapper.toEntity(warehouse);
        em.persist(warehouseEntity);
        em.flush();
        em.clear();

        ProductJpaEntity managedProduct = em.find(ProductJpaEntity.class, product.getProductId());
        WarehouseJpaEntity managedWarehouse = em.find(WarehouseJpaEntity.class, warehouse.getWarehouseId());

        UUID inventoryId = UUID.randomUUID();
        InventoryJpaEntity initialInventory = new InventoryJpaEntity(
                inventoryId, managedProduct, managedWarehouse, 50, 0,
                application.domain.enums.InventoryStatus.AVAILABLE
        );
        em.persist(initialInventory);
        em.flush();
        em.clear();

        InventoryJpaEntity loaded = inventoryRepository.findById(inventoryId).orElseThrow();
        assertEquals(0L, loaded.getVersion());
        assertNotNull(loaded.getCreatedAt());
        assertNotNull(loaded.getUpdatedAt());

        // Prepare stale copy with version = 0
        InventoryJpaEntity staleInventory = new InventoryJpaEntity(
                inventoryId, managedProduct, managedWarehouse, 10, 40,
                application.domain.enums.InventoryStatus.AVAILABLE
        );
        staleInventory.setVersion(0L);

        // First update via adapter reserves 5 units -> increments version to 1
        Inventory domainInventory = inventoryAdapter.findById(inventoryId).orElseThrow();
        domainInventory.reserve(5);
        inventoryAdapter.save(domainInventory);
        em.flush();
        em.clear();

        InventoryJpaEntity afterReserve = inventoryRepository.findById(inventoryId).orElseThrow();
        assertEquals(1L, afterReserve.getVersion());
        assertEquals(45, afterReserve.getAvailableQuantity());
        assertEquals(5, afterReserve.getReservedQuantity());

        // Concurrent merge with stale version 0 must throw OptimisticLockException
        assertThrows(OptimisticLockException.class, () -> {
            em.getEntityManager().merge(staleInventory);
            em.flush();
        });
    }

    @Test
    void jpaAuditing_populatesCreatedAtAndUpdatedAtAcrossMainEntities() {
        Buyer buyer = Buyer.reconstitute(
                UUID.randomUUID(),
                "Carlos Gomez",
                new Email("carlos@example.com"),
                new IdentityDocument("CC", "11223344"),
                new Address("Calle 10", "Bogota", "Cundinamarca", "Colombia", "110111", null),
                List.of(),
                BuyerCommercialStatus.ENABLED,
                UserStatus.ACTIVE
        );
        BuyerEntity buyerEntity = (BuyerEntity) userMapper.toEntity(buyer);
        em.persist(buyerEntity);

        Product product = new Product(
                UUID.randomUUID(),
                "Laptop Pro",
                new ProductVariant("RAM", "32GB"),
                new Money(new BigDecimal("1500.00"), USD),
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );
        productAdapter.save(product);
        em.flush();
        em.clear();

        BuyerEntity savedBuyer = em.find(BuyerEntity.class, buyer.getUserId());
        assertNotNull(savedBuyer.getCreatedAt(), "UserEntity createdAt must be populated by JPA Auditing");
        assertNotNull(savedBuyer.getUpdatedAt(), "UserEntity updatedAt must be populated by JPA Auditing");

        Order order = Order.reconstitute(
                UUID.randomUUID(),
                buyer,
                List.of(new OrderItem(product, 1, new Money(new BigDecimal("1500.00"), USD))),
                buyer.getPrimaryAddress(),
                OrderStatus.PENDING_PAYMENT,
                LocalDate.now()
        );
        orderAdapter.save(order);
        em.flush();
        em.clear();

        OrderJpaEntity savedOrder = em.find(OrderJpaEntity.class, order.getOrderId());
        assertNotNull(savedOrder.getCreatedAt(), "OrderJpaEntity createdAt must be populated by JPA Auditing");
        assertNotNull(savedOrder.getUpdatedAt(), "OrderJpaEntity updatedAt must be populated by JPA Auditing");
        LocalDateTime initialOrderCreatedAt = savedOrder.getCreatedAt();

        Invoice invoice = new Invoice(
                UUID.randomUUID(),
                order,
                LocalDate.now(),
                new Money(new BigDecimal("1500.00"), USD),
                new Money(new BigDecimal("285.00"), USD)
        );
        InvoiceJpaEntity invoiceEntity = invoiceMapper.toEntity(invoice, savedOrder);
        em.persist(invoiceEntity);
        em.flush();
        em.clear();

        InvoiceJpaEntity savedInvoice = em.find(InvoiceJpaEntity.class, invoice.getInvoiceId());
        assertNotNull(savedInvoice.getCreatedAt(), "InvoiceJpaEntity createdAt must be populated by JPA Auditing");
        assertNotNull(savedInvoice.getUpdatedAt(), "InvoiceJpaEntity updatedAt must be populated by JPA Auditing");

        // Advance order state and verify createdAt is preserved while updatedAt is >= createdAt
        Order loadedOrder = orderAdapter.findById(order.getOrderId()).orElseThrow();
        loadedOrder.advanceTo(OrderStatus.PAID);
        orderAdapter.save(loadedOrder);
        em.flush();
        em.clear();

        OrderJpaEntity updatedOrder = em.find(OrderJpaEntity.class, order.getOrderId());
        assertEquals(initialOrderCreatedAt, updatedOrder.getCreatedAt(), "createdAt must be immutable across updates");
        assertTrue(!updatedOrder.getUpdatedAt().isBefore(initialOrderCreatedAt),
                "updatedAt must be on or after createdAt");
    }
}

package application.adapters.config;

import application.adapters.persistence.jpa.OrderJpaAdapter;
import application.adapters.persistence.jpa.ProductJpaAdapter;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.adapters.rest.controllers.AdministratorRestController;
import application.adapters.rest.controllers.BuyerRestController;
import application.adapters.rest.controllers.SellerRestController;
import application.adapters.rest.mappers.CartRestMapper;
import application.adapters.rest.mappers.InvoiceRestMapper;
import application.adapters.rest.mappers.OperationRestMapper;
import application.adapters.rest.mappers.OrderRestMapper;
import application.adapters.rest.mappers.ProductRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.adapters.useCases.AdministratorUseCaseImpl;
import application.adapters.useCases.BuyerUseCaseImpl;
import application.adapters.useCases.LogisticsOperatorUseCaseImpl;
import application.adapters.useCases.PublicAccessUseCaseImpl;
import application.adapters.useCases.SellerUseCaseImpl;
import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserStatus;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.ports.in.AdministratorPort;
import application.domain.ports.in.BuyerPort;
import application.domain.ports.in.PublicAccessPort;
import application.domain.ports.in.SellerPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.AuthorizeCustomerRegistrationService;
import application.domain.services.user.UniqueIdentityValidationService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import application.domain.valueobjects.PageResult;
import application.domain.valueobjects.ProductVariant;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaginationAndCacheIntegrationTest {

    @Configuration
    static class CacheTestConfig {
        @Bean
        public ProductRepositoryPort productRepositoryPort() {
            return mock(ProductRepositoryPort.class);
        }

        @Bean
        public OrderRepositoryPort orderRepositoryPort() {
            return mock(OrderRepositoryPort.class);
        }

        @Bean
        public UserRepositoryPort userRepositoryPort() {
            return mock(UserRepositoryPort.class);
        }

        @Bean
        public SellerPort sellerPort(UserRepositoryPort userRepositoryPort,
                                     ProductRepositoryPort productRepositoryPort,
                                     OrderRepositoryPort orderRepositoryPort) {
            return new SellerUseCaseImpl(
                    userRepositoryPort,
                    productRepositoryPort,
                    orderRepositoryPort,
                    new UniqueIdentityValidationService()
            );
        }

        @Bean
        public PublicAccessPort publicAccessPort(UserRepositoryPort userRepositoryPort,
                                                 ProductRepositoryPort productRepositoryPort) {
            return new PublicAccessUseCaseImpl(
                    userRepositoryPort,
                    productRepositoryPort,
                    null,
                    null,
                    new UniqueIdentityValidationService(),
                    new AuthorizeCustomerRegistrationService()
            );
        }
    }

    @Test
    void catalogCache_cachesByPageAndSize_andEvictsOnCreatePriceAndStatusUpdates() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                CacheConfig.class, CacheTestConfig.class)) {

            SellerPort sellerPort = context.getBean(SellerPort.class);
            PublicAccessPort publicAccessPort = context.getBean(PublicAccessPort.class);
            ProductRepositoryPort productRepository = context.getBean(ProductRepositoryPort.class);
            CacheManager cacheManager = context.getBean(CacheManager.class);

            UUID productId = UUID.randomUUID();
            Product product = new Product(
                    productId,
                    "Monitor 4K",
                    new ProductVariant("Size", "27-inch"),
                    new Money(BigDecimal.valueOf(350), Currency.getInstance("USD")),
                    ProductStatus.PUBLISHED,
                    ProductType.PHYSICAL
            );
            PageResult<Product> pageResult = PageResult.of(List.of(product), 0, 10);

            when(productRepository.findAll(0, 10, "name", "ASC")).thenReturn(pageResult);
            when(productRepository.findByStatus(ProductStatus.PUBLISHED, 1, 5, "name", "ASC"))
                    .thenReturn(PageResult.of(List.of(product), 1, 5));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            // 1. First query populates cache key "0-10"
            PageResult<Product> firstCall = sellerPort.getAllProducts(0, 10);
            PageResult<Product> secondCall = sellerPort.getAllProducts(0, 10);
            assertEquals(1, firstCall.content().size());
            assertEquals(1, secondCall.content().size());
            verify(productRepository, times(1)).findAll(0, 10, "name", "ASC");

            Cache publicCache = cacheManager.getCache("products_catalog");
            Cache sellerCache = cacheManager.getCache("seller_products");
            assertNotNull(publicCache);
            assertNotNull(sellerCache);
            assertNotNull(sellerCache.get("0-10"));

            // 2. Public catalog query populates cache key "1-5" in products_catalog
            publicAccessPort.getPublishedCatalog(1, 5);
            publicAccessPort.getPublishedCatalog(1, 5);
            verify(productRepository, times(1)).findByStatus(ProductStatus.PUBLISHED, 1, 5, "name", "ASC");
            assertNotNull(publicCache.get("1-5"));

            // 3. Creating a product evicts all entries in both caches
            sellerPort.createProduct("Mouse Pro", "Color", "Black",
                    BigDecimal.valueOf(50), "USD", ProductStatus.PUBLISHED, ProductType.PHYSICAL);
            assertNull(sellerCache.get("0-10"));
            assertNull(publicCache.get("1-5"));

            // Re-populate cache
            sellerPort.getAllProducts(0, 10);
            verify(productRepository, times(2)).findAll(0, 10, "name", "ASC");
            assertNotNull(sellerCache.get("0-10"));

            // 4. Updating product price evicts cache
            sellerPort.updateProductPrice(productId, BigDecimal.valueOf(320), "USD");
            assertNull(sellerCache.get("0-10"));

            // Re-populate cache
            sellerPort.getAllProducts(0, 10);
            verify(productRepository, times(3)).findAll(0, 10, "name", "ASC");
            assertNotNull(sellerCache.get("0-10"));

            // 5. Updating product status evicts cache
            sellerPort.updateProductStatus(productId, ProductStatus.SUSPENDED);
            assertNull(sellerCache.get("0-10"));
        }
    }

    @Test
    void jpaAdapters_paginateAndSortProductsAndOrdersCorrectly() {
        SpringDataJpaProductRepository productJpaRepo = mock(SpringDataJpaProductRepository.class);
        SpringDataJpaOrderRepository orderJpaRepo = mock(SpringDataJpaOrderRepository.class);
        ProductJpaMapper productMapper = new ProductJpaMapper();
        OrderJpaMapper orderMapper = new OrderJpaMapper(new UserJpaMapper(), productMapper);

        ProductJpaAdapter productAdapter = new ProductJpaAdapter(productJpaRepo, productMapper);
        OrderJpaAdapter orderAdapter = new OrderJpaAdapter(orderJpaRepo, productJpaRepo, orderMapper);

        Product p1 = new Product(UUID.randomUUID(), "Keyboard", new ProductVariant("Switch", "Red"),
                new Money(BigDecimal.valueOf(100), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        ProductJpaEntity p1Entity = productMapper.toEntity(p1);

        Page<ProductJpaEntity> springProductPage = new PageImpl<>(
                List.of(p1Entity),
                PageRequest.of(0, 5),
                12L
        );
        when(productJpaRepo.findAll(any(Pageable.class))).thenReturn(springProductPage);

        PageResult<Product> productPageResult = productAdapter.findAll(0, 5, "price", "DESC");
        assertEquals(1, productPageResult.content().size());
        assertEquals(0, productPageResult.page());
        assertEquals(5, productPageResult.size());
        assertEquals(12L, productPageResult.totalElements());
        assertEquals(3, productPageResult.totalPages());
        assertFalse(productPageResult.last());

        UUID buyerId = UUID.randomUUID();
        Buyer buyer = Buyer.reconstitute(
                buyerId,
                "Buyer Test",
                new Email("buyer@nexus.com"),
                new IdentityDocument("CC", "123456"),
                new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null),
                List.of(),
                BuyerCommercialStatus.ENABLED,
                UserStatus.ACTIVE
        );
        Order order = Order.reconstitute(
                UUID.randomUUID(),
                buyer,
                List.of(new OrderItem(p1, 1, p1.getPrice())),
                buyer.getPrimaryAddress(),
                OrderStatus.PAID,
                LocalDate.now()
        );
        OrderJpaEntity orderEntity = orderMapper.toJpaEntity(order, prod -> p1Entity);
        Page<OrderJpaEntity> springOrderPage = new PageImpl<>(
                List.of(orderEntity),
                PageRequest.of(0, 10),
                1L
        );
        when(orderJpaRepo.findByBuyer_UserId(eq(buyerId), any(Pageable.class))).thenReturn(springOrderPage);

        PageResult<Order> orderPageResult = orderAdapter.findByBuyerId(buyerId, 0, 10, "orderDate", "DESC");
        assertEquals(1, orderPageResult.content().size());
        assertEquals(1L, orderPageResult.totalElements());
        assertEquals(1, orderPageResult.totalPages());
        assertTrue(orderPageResult.last());
    }

    @Test
    void restControllers_returnStandardizedPageResponseDTO() throws Exception {
        BuyerPort buyerPort = mock(BuyerPort.class);
        SellerPort sellerPort = mock(SellerPort.class);
        AdministratorPort adminPort = mock(AdministratorPort.class);

        BuyerRestController buyerController = new BuyerRestController(
                buyerPort, new UserRestMapper(), new CartRestMapper(), new OrderRestMapper());
        SellerRestController sellerController = new SellerRestController(
                sellerPort, new ProductRestMapper(), new UserRestMapper(), new OrderRestMapper());
        AdministratorRestController adminController = new AdministratorRestController(
                adminPort, new UserRestMapper(), new InvoiceRestMapper(), new OperationRestMapper());

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(buyerController, sellerController, adminController).build();

        UUID buyerId = UUID.randomUUID();
        Product product = new Product(UUID.randomUUID(), "SSD 1TB", new ProductVariant("Interface", "NVMe"),
                new Money(BigDecimal.valueOf(120), Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);

        when(sellerPort.getAllProducts(0, 10)).thenReturn(PageResult.of(List.of(product), 0, 10));
        when(buyerPort.getOrdersByBuyerId(buyerId, 0, 10, "orderDate", "DESC"))
                .thenReturn(PageResult.of(List.of(), 0, 10));
        when(adminPort.listAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/sellers/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("SSD 1TB"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        mockMvc.perform(get("/api/buyers/{buyerId}/orders", buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.last").value(true));

        mockMvc.perform(get("/api/admin/users").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void useCaseImplementations_enforceTransactionalBoundaries() throws NoSuchMethodException {
        assertTrue(BuyerUseCaseImpl.class.getAnnotation(Transactional.class).readOnly());
        assertTrue(SellerUseCaseImpl.class.getAnnotation(Transactional.class).readOnly());
        assertTrue(AdministratorUseCaseImpl.class.getAnnotation(Transactional.class).readOnly());
        assertTrue(LogisticsOperatorUseCaseImpl.class.getAnnotation(Transactional.class).readOnly());

        Method createOrder = BuyerUseCaseImpl.class.getMethod("createOrderFromCart", UUID.class, Address.class);
        assertFalse(createOrder.getAnnotation(Transactional.class).readOnly());

        Method cancelOrder = BuyerUseCaseImpl.class.getMethod("cancelOrder", UUID.class);
        assertFalse(cancelOrder.getAnnotation(Transactional.class).readOnly());

        Method registerStock = SellerUseCaseImpl.class.getMethod("registerInitialStock", UUID.class, UUID.class, UUID.class, int.class);
        assertFalse(registerStock.getAnnotation(Transactional.class).readOnly());

        Method updateDeliveryStatus = LogisticsOperatorUseCaseImpl.class.getMethod("updateDeliveryStatus", UUID.class, OrderStatus.class);
        assertFalse(updateDeliveryStatus.getAnnotation(Transactional.class).readOnly());
    }
}

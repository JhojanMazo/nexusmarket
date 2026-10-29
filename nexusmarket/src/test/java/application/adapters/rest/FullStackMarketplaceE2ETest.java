package application.adapters.rest;

import application.adapters.config.JpaAuditingConfig;
import application.adapters.events.SpringEventPublisherAdapter;
import application.adapters.persistence.jpa.InventoryJpaAdapter;
import application.adapters.persistence.jpa.InventoryMovementJpaAdapter;
import application.adapters.persistence.jpa.InvoiceJpaAdapter;
import application.adapters.persistence.jpa.OperationJpaAdapter;
import application.adapters.persistence.jpa.OrderJpaAdapter;
import application.adapters.persistence.jpa.ProductJpaAdapter;
import application.adapters.persistence.jpa.RefundJpaAdapter;
import application.adapters.persistence.jpa.ReturnRequestJpaAdapter;
import application.adapters.persistence.jpa.ShipmentJpaAdapter;
import application.adapters.persistence.jpa.ShoppingCartJpaAdapter;
import application.adapters.persistence.jpa.UserJpaAdapter;
import application.adapters.persistence.jpa.WarehouseJpaAdapter;
import application.adapters.persistence.jpa.mappers.CartJpaMapper;
import application.adapters.persistence.jpa.mappers.InventoryJpaMapper;
import application.adapters.persistence.jpa.mappers.InventoryMovementJpaMapper;
import application.adapters.persistence.jpa.mappers.InvoiceJpaMapper;
import application.adapters.persistence.jpa.mappers.OperationJpaMapper;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.mappers.RefundJpaMapper;
import application.adapters.persistence.jpa.mappers.ReturnRequestJpaMapper;
import application.adapters.persistence.jpa.mappers.ShipmentJpaMapper;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.mappers.WarehouseJpaMapper;
import application.adapters.rest.controllers.AdministratorRestController;
import application.adapters.rest.controllers.AuthRestController;
import application.adapters.rest.controllers.BuyerRestController;
import application.adapters.rest.controllers.LogisticsOperatorRestController;
import application.adapters.rest.controllers.SellerRestController;
import application.adapters.rest.controllers.SupervisorRestController;
import application.adapters.rest.exception.GlobalExceptionHandler;
import application.adapters.rest.mappers.CartRestMapper;
import application.adapters.rest.mappers.InventoryRestMapper;
import application.adapters.rest.mappers.InvoiceRestMapper;
import application.adapters.rest.mappers.OperationRestMapper;
import application.adapters.rest.mappers.OrderRestMapper;
import application.adapters.rest.mappers.ProductRestMapper;
import application.adapters.rest.mappers.RefundRestMapper;
import application.adapters.rest.mappers.ReturnRequestRestMapper;
import application.adapters.rest.mappers.ShipmentRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.adapters.rest.mappers.WarehouseRestMapper;
import application.adapters.security.BCryptPasswordServiceAdapter;
import application.adapters.security.JwtAuthenticationFilter;
import application.adapters.security.JwtProvider;
import application.adapters.useCases.AdministratorUseCaseImpl;
import application.adapters.useCases.BuyerUseCaseImpl;
import application.adapters.useCases.LogisticsOperatorUseCaseImpl;
import application.adapters.useCases.PublicAccessUseCaseImpl;
import application.adapters.useCases.SellerUseCaseImpl;
import application.adapters.useCases.SupervisorUseCaseImpl;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.domain.ports.out.OperationRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.ports.out.ShoppingCartRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.AuthorizeCustomerRegistrationService;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.authorization.ValidateSellerOwnershipService;
import application.domain.services.billing.AdministrativeReportsConsolidationService;
import application.domain.services.billing.RefundsManagementService;
import application.domain.services.inventory.CriticalStockValidationService;
import application.domain.services.inventory.InventoryReservationService;
import application.domain.services.inventory.WarehouseInformationControlService;
import application.domain.services.logistics.LogisticsShippingManagementService;
import application.domain.services.logistics.ReturnsAdministrationService;
import application.domain.services.product.ProductTypeDifferentiationService;
import application.domain.services.user.PrimaryAddressManagementService;
import application.domain.services.user.SecondaryAddressManagementService;
import application.domain.services.user.UniqueIdentityValidationService;
import application.infrastructure.seed.DatabaseSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Suite End-to-End (E2E) Full Stack de NexusMarket sobre persistencia relacional real (H2),
 * casos de uso transaccionales, filtro de seguridad JWT + RBAC y controladores REST reales.
 * Verifica los 10 escenarios obligatorios del Marketplace.
 */
@DataJpaTest(properties = "spring.datasource.url=jdbc:h2:mem:fullstack_e2e_it;DB_CLOSE_DELAY=-1")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaAuditingConfig.class,
        UserJpaAdapter.class, UserJpaMapper.class,
        ProductJpaAdapter.class, ProductJpaMapper.class,
        WarehouseJpaAdapter.class, WarehouseJpaMapper.class,
        InventoryJpaAdapter.class, InventoryJpaMapper.class,
        InventoryMovementJpaAdapter.class, InventoryMovementJpaMapper.class,
        ShoppingCartJpaAdapter.class, CartJpaMapper.class,
        OrderJpaAdapter.class, OrderJpaMapper.class,
        InvoiceJpaAdapter.class, InvoiceJpaMapper.class,
        ShipmentJpaAdapter.class, ShipmentJpaMapper.class,
        ReturnRequestJpaAdapter.class, ReturnRequestJpaMapper.class,
        RefundJpaAdapter.class, RefundJpaMapper.class,
        OperationJpaAdapter.class, OperationJpaMapper.class
})
class FullStackMarketplaceE2ETest {

    @Autowired private UserRepositoryPort userRepository;
    @Autowired private ProductRepositoryPort productRepository;
    @Autowired private WarehouseRepositoryPort warehouseRepository;
    @Autowired private InventoryRepositoryPort inventoryRepository;
    @Autowired private InventoryMovementRepositoryPort inventoryMovementRepository;
    @Autowired private ShoppingCartRepositoryPort cartRepository;
    @Autowired private OrderRepositoryPort orderRepository;
    @Autowired private InvoiceRepositoryPort invoiceRepository;
    @Autowired private ShipmentRepositoryPort shipmentRepository;
    @Autowired private ReturnRequestRepositoryPort returnRequestRepository;
    @Autowired private RefundRepositoryPort refundRepository;
    @Autowired private OperationRepositoryPort operationRepository;
    @Autowired private ApplicationEventPublisher applicationEventPublisher;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpFullStackContext() {
        // 1. Seed default roles, warehouses, products and initial stock
        DatabaseSeeder seeder = new DatabaseSeeder(
                userRepository, productRepository, warehouseRepository, inventoryRepository);
        seeder.run();

        // 2. Wire real security, domain services and use cases
        JwtProvider jwtProvider = new JwtProvider(
                "NexusMarketE2EFullStackCryptographicSecretKey2026Valid32Bytes!", 86400);
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtProvider);
        BCryptPasswordServiceAdapter passwordService = new BCryptPasswordServiceAdapter();
        UniqueIdentityValidationService uniqueIdentityService = new UniqueIdentityValidationService();
        SpringEventPublisherAdapter eventPublisher = new SpringEventPublisherAdapter(applicationEventPublisher);

        PublicAccessUseCaseImpl publicAccessUseCase = new PublicAccessUseCaseImpl(
                userRepository,
                productRepository,
                inventoryRepository,
                jwtProvider,
                passwordService,
                uniqueIdentityService,
                new AuthorizeCustomerRegistrationService()
        );

        BuyerUseCaseImpl buyerUseCase = new BuyerUseCaseImpl(
                userRepository,
                cartRepository,
                productRepository,
                orderRepository,
                inventoryRepository,
                returnRequestRepository,
                uniqueIdentityService,
                new ReturnsAdministrationService(),
                new InventoryReservationService(),
                new CriticalStockValidationService(),
                new ProductTypeDifferentiationService(),
                new ValidateBuyerOwnershipService(),
                new PrimaryAddressManagementService(),
                new SecondaryAddressManagementService(),
                eventPublisher
        );

        SellerUseCaseImpl sellerUseCase = new SellerUseCaseImpl(
                userRepository,
                productRepository,
                orderRepository,
                warehouseRepository,
                inventoryRepository,
                uniqueIdentityService,
                new ValidateSellerOwnershipService()
        );

        LogisticsOperatorUseCaseImpl logisticsUseCase = new LogisticsOperatorUseCaseImpl(
                orderRepository,
                shipmentRepository,
                inventoryRepository,
                inventoryMovementRepository,
                userRepository,
                new LogisticsShippingManagementService()
        );

        SupervisorUseCaseImpl supervisorUseCase = new SupervisorUseCaseImpl(
                orderRepository,
                operationRepository,
                warehouseRepository,
                inventoryRepository,
                invoiceRepository,
                shipmentRepository,
                returnRequestRepository,
                refundRepository,
                productRepository,
                userRepository,
                new AdministrativeReportsConsolidationService()
        );

        AdministratorUseCaseImpl adminUseCase = new AdministratorUseCaseImpl(
                userRepository,
                invoiceRepository,
                orderRepository,
                productRepository,
                warehouseRepository,
                inventoryRepository,
                shipmentRepository,
                returnRequestRepository,
                refundRepository,
                operationRepository,
                uniqueIdentityService,
                new WarehouseInformationControlService(),
                new ReturnsAdministrationService(),
                new RefundsManagementService(),
                new AdministrativeReportsConsolidationService()
        );

        // 3. Wire real REST controllers + GlobalExceptionHandler + JwtAuthenticationFilter
        UserRestMapper userRestMapper = new UserRestMapper();
        ProductRestMapper productRestMapper = new ProductRestMapper();
        CartRestMapper cartRestMapper = new CartRestMapper();
        OrderRestMapper orderRestMapper = new OrderRestMapper();
        WarehouseRestMapper warehouseRestMapper = new WarehouseRestMapper();
        InventoryRestMapper inventoryRestMapper = new InventoryRestMapper();
        ShipmentRestMapper shipmentRestMapper = new ShipmentRestMapper();
        ReturnRequestRestMapper returnRequestRestMapper = new ReturnRequestRestMapper();
        RefundRestMapper refundRestMapper = new RefundRestMapper();
        InvoiceRestMapper invoiceRestMapper = new InvoiceRestMapper();
        OperationRestMapper operationRestMapper = new OperationRestMapper();

        AuthRestController authController = new AuthRestController(publicAccessUseCase, userRestMapper, productRestMapper);
        BuyerRestController buyerController = new BuyerRestController(
                buyerUseCase, userRestMapper, cartRestMapper, orderRestMapper, returnRequestRestMapper, productRestMapper);
        SellerRestController sellerController = new SellerRestController(
                sellerUseCase, productRestMapper, userRestMapper, orderRestMapper, warehouseRestMapper, inventoryRestMapper);
        LogisticsOperatorRestController logisticsController = new LogisticsOperatorRestController(
                logisticsUseCase, orderRestMapper, shipmentRestMapper, inventoryRestMapper);
        SupervisorRestController supervisorController = new SupervisorRestController(
                supervisorUseCase, orderRestMapper, operationRestMapper, warehouseRestMapper,
                inventoryRestMapper, shipmentRestMapper, returnRequestRestMapper, refundRestMapper);
        AdministratorRestController adminController = new AdministratorRestController(
                adminUseCase, userRestMapper, invoiceRestMapper, operationRestMapper,
                warehouseRestMapper, returnRequestRestMapper, refundRestMapper, orderRestMapper, productRestMapper);

        this.mockMvc = MockMvcBuilders.standaloneSetup(
                        authController,
                        buyerController,
                        sellerController,
                        logisticsController,
                        supervisorController,
                        adminController
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(jwtFilter)
                .build();
    }

    @Test
    void fullStackMarketplace_verifiesStaticWebAssetsAndExecutesAll10EndToEndScenarios() throws Exception {
        // 0. Verificar activos estáticos del Frontend integrados en el classpath de Spring Boot
        ClassPathResource indexHtml = new ClassPathResource("static/index.html");
        ClassPathResource stylesCss = new ClassPathResource("static/css/styles.css");
        ClassPathResource appJs = new ClassPathResource("static/js/app.js");
        assertTrue(indexHtml.exists(), "index.html debe existir en static/");
        assertTrue(stylesCss.exists(), "styles.css debe existir en static/css/");
        assertTrue(appJs.exists(), "app.js debe existir en static/js/");
        String htmlContent = new String(indexHtml.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(htmlContent.contains("NexusMarket") && htmlContent.contains("app-main"));

        // =========================================================================
        // ESCENARIO 1: Un usuario nuevo se registra e inicia sesión recibiendo JWT
        // =========================================================================
        String registerPayload = """
                {
                  "fullName": "Carlos Andrés Restrepo",
                  "email": "carlos.e2e@nexusmarket.com",
                  "password": "Secret123!",
                  "documentType": "CC",
                  "documentNumber": "1098765432",
                  "primaryAddress": {
                    "street": "Carrera 7 # 72-41",
                    "city": "Bogotá",
                    "stateOrProvince": "Cundinamarca",
                    "country": "Colombia",
                    "postalCode": "110221",
                    "reference": "Oficina 801"
                  }
                }
                """;

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("carlos.e2e@nexusmarket.com"))
                .andExpect(jsonPath("$.role").value("BUYER"))
                .andReturn();

        String buyerId = extractJsonField(regResult.getResponse().getContentAsString(), "userId");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"carlos.e2e@nexusmarket.com\",\"password\":\"Secret123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("BUYER"))
                .andReturn();

        String buyerToken = extractJsonField(loginResult.getResponse().getContentAsString(), "token");

        // =========================================================================
        // ESCENARIO 2: Consulta el catálogo con filtros, abre un producto y su stock
        // =========================================================================
        MvcResult catalogResult = mockMvc.perform(get("/api/auth/catalog")
                        .param("search", "MacBook")
                        .param("type", "PHYSICAL")
                        .param("sortBy", "price")
                        .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("MacBook Pro M3 Max"))
                .andReturn();

        String macbookId = extractJsonField(catalogResult.getResponse().getContentAsString(), "id");

        mockMvc.perform(get("/api/auth/catalog/{id}", macbookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MacBook Pro M3 Max"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(get("/api/auth/catalog/{id}/stock", macbookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(50))
                .andExpect(jsonPath("$.inStock").value(true));

        // =========================================================================
        // ESCENARIO 3: Obtiene su carrito y agrega productos
        // =========================================================================
        MvcResult cartResult = mockMvc.perform(get("/api/buyers/{buyerId}/cart", buyerId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andReturn();

        String cartId = extractJsonField(cartResult.getResponse().getContentAsString(), "cartId");

        mockMvc.perform(post("/api/buyers/carts/{cartId}/items", cartId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": "%s",
                                  "quantity": 1,
                                  "unitPrice": 14500000,
                                  "currency": "COP"
                                }
                                """.formatted(macbookId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(1))
                .andExpect(jsonPath("$.subtotal").value(14500000));

        // =========================================================================
        // ESCENARIO 4: Modifica cantidades en el carrito y revisa el total
        // =========================================================================
        mockMvc.perform(put("/api/buyers/carts/{cartId}/items/{productId}", cartId, macbookId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.subtotal").value(29000000));

        // =========================================================================
        // ESCENARIO 5: Completa un pedido desde el carrito y procesa el pago
        // =========================================================================
        MvcResult orderCreateResult = mockMvc.perform(post("/api/buyers/orders")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": "%s",
                                  "deliveryAddress": {
                                    "street": "Carrera 7 # 72-41",
                                    "city": "Bogotá",
                                    "stateOrProvince": "Cundinamarca",
                                    "country": "Colombia",
                                    "postalCode": "110221",
                                    "reference": "Oficina 801"
                                  }
                                }
                                """.formatted(cartId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(29000000))
                .andReturn();

        String orderId = extractJsonField(orderCreateResult.getResponse().getContentAsString(), "orderId");

        mockMvc.perform(post("/api/buyers/orders/{orderId}/pay", orderId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "%s",
                                  "amount": 29000000,
                                  "currency": "COP",
                                  "paymentMethod": "CREDIT_CARD"
                                }
                                """.formatted(orderId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // Verificar que el stock disponible bajó de 50 a 48 unidades tras reservar 2
        mockMvc.perform(get("/api/auth/catalog/{id}/stock", macbookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(48));

        // =========================================================================
        // ESCENARIO 6: Consulta el pedido en su historial y prueba cancelación
        // =========================================================================
        mockMvc.perform(get("/api/buyers/{buyerId}/orders", buyerId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderId").value(orderId))
                .andExpect(jsonPath("$.content[0].status").value("PAID"))
                .andExpect(jsonPath("$.content[0].items[0].quantity").value(2));

        // Crear una segunda orden y cancelarla antes del envío
        MvcResult secondCartRes = mockMvc.perform(get("/api/buyers/{buyerId}/cart", buyerId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andReturn();
        String secondCartId = extractJsonField(secondCartRes.getResponse().getContentAsString(), "cartId");

        mockMvc.perform(post("/api/buyers/carts/{cartId}/items", secondCartId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId": "%s", "quantity": 1, "unitPrice": 14500000, "currency": "COP"}
                                """.formatted(macbookId)))
                .andExpect(status().isOk());

        MvcResult secondOrderRes = mockMvc.perform(post("/api/buyers/orders")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": "%s",
                                  "deliveryAddress": {
                                    "street": "Carrera 7 # 72-41",
                                    "city": "Bogotá",
                                    "stateOrProvince": "Cundinamarca",
                                    "country": "Colombia",
                                    "postalCode": "110221"
                                  }
                                }
                                """.formatted(secondCartId)))
                .andExpect(status().isCreated())
                .andReturn();
        String secondOrderId = extractJsonField(secondOrderRes.getResponse().getContentAsString(), "orderId");

        mockMvc.perform(post("/api/buyers/orders/{orderId}/cancel", secondOrderId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // =========================================================================
        // ESCENARIO 7: Un vendedor autenticado administra un producto propio y stock
        // =========================================================================
        MvcResult sellerLoginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"seller@nexusmarket.com\",\"password\":\"NexusMarket2026!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String sellerToken = extractJsonField(sellerLoginRes.getResponse().getContentAsString(), "token");
        String sellerId = extractJsonField(sellerLoginRes.getResponse().getContentAsString(), "userId");

        MvcResult newProdRes = mockMvc.perform(post("/api/sellers/products")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Estación de Trabajo GPU Pro",
                                  "variantAttribute": "VRAM",
                                  "variantValue": "48GB GDDR6",
                                  "price": 18900000,
                                  "currency": "COP",
                                  "status": "PUBLISHED",
                                  "type": "PHYSICAL"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Estación de Trabajo GPU Pro"))
                .andReturn();
        String newProdId = extractJsonField(newProdRes.getResponse().getContentAsString(), "id");

        mockMvc.perform(patch("/api/sellers/products/{productId}/price", newProdId)
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\": 17900000, \"currency\": \"COP\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(17900000));

        MvcResult sellerWhRes = mockMvc.perform(get("/api/sellers/{sellerId}/warehouses", sellerId)
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andReturn();
        String sellerWhId = extractJsonField(sellerWhRes.getResponse().getContentAsString(), "warehouseId");

        mockMvc.perform(post("/api/sellers/{sellerId}/inventory/initial", sellerId)
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": "%s",
                                  "warehouseId": "%s",
                                  "quantity": 15
                                }
                                """.formatted(newProdId, sellerWhId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.availableQuantity").value(15));

        // =========================================================================
        // ESCENARIO 8: Usuario no autorizado intenta acceder a recursos protegidos
        // =========================================================================
        // 8a. Sin token -> 401 Unauthorized
        mockMvc.perform(get("/api/buyers/{buyerId}", buyerId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());

        // 8b. Comprador intentando acceder al panel administrativo o de vendedor -> 403 Forbidden
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/sellers/products")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        // 8c. Comprador intentando consultar el perfil de otro comprador -> 403 Forbidden
        MvcResult seededBuyerLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"buyer@nexusmarket.com\",\"password\":\"NexusMarket2026!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String otherBuyerId = extractJsonField(seededBuyerLogin.getResponse().getContentAsString(), "userId");

        mockMvc.perform(get("/api/buyers/{buyerId}", otherBuyerId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isForbidden());

        // =========================================================================
        // ESCENARIO 9: Logística despacha/entrega, comprador solicita devolución y
        //              Administrador consulta operaciones y aprueba la devolución
        // =========================================================================
        MvcResult logisticsLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"logistics@nexusmarket.com\",\"password\":\"NexusMarket2026!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String logisticsToken = extractJsonField(logisticsLogin.getResponse().getContentAsString(), "token");
        String logisticsId = extractJsonField(logisticsLogin.getResponse().getContentAsString(), "userId");

        MvcResult shipmentRes = mockMvc.perform(post("/api/logistics/shipments/dispatch")
                        .header("Authorization", "Bearer " + logisticsToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "%s",
                                  "logisticsOperatorId": "%s"
                                }
                                """.formatted(orderId, logisticsId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DISPATCHED"))
                .andReturn();
        String shipmentId = extractJsonField(shipmentRes.getResponse().getContentAsString(), "shipmentId");

        mockMvc.perform(patch("/api/logistics/shipments/{shipmentId}/deliver", shipmentId)
                        .header("Authorization", "Bearer " + logisticsToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        // Comprador solicita devolución sobre su orden entregada
        MvcResult returnRes = mockMvc.perform(post("/api/buyers/orders/{orderId}/return", orderId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Empaque con sello abierto\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn();
        String returnRequestId = extractJsonField(returnRes.getResponse().getContentAsString(), "requestId");

        // Administrador inicia sesión, consulta reporte consolidado y aprueba la devolución
        MvcResult adminLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@nexusmarket.com\",\"password\":\"NexusMarket2026!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = extractJsonField(adminLogin.getResponse().getContentAsString(), "token");

        mockMvc.perform(get("/api/admin/reports/consolidated")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.totalOrders").isNumber())
                .andExpect(jsonPath("$.totalProducts").isNumber());

        mockMvc.perform(patch("/api/admin/returns/{requestId}/approve", returnRequestId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // =========================================================================
        // ESCENARIO 10: Operación inválida produce error controlado sin corromper datos
        // =========================================================================
        // Intentar crear orden desde un carrito vacío -> 400 Bad Request
        MvcResult emptyCartRes = mockMvc.perform(get("/api/buyers/{buyerId}/cart", buyerId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andReturn();
        String emptyCartId = extractJsonField(emptyCartRes.getResponse().getContentAsString(), "cartId");

        mockMvc.perform(post("/api/buyers/orders")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": "%s",
                                  "deliveryAddress": {
                                    "street": "Carrera 7",
                                    "city": "Bogotá",
                                    "stateOrProvince": "Cundinamarca",
                                    "country": "Colombia",
                                    "postalCode": "110221"
                                  }
                                }
                                """.formatted(emptyCartId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        // Intentar agregar cantidad <= 0 al carrito -> 400 Bad Request
        mockMvc.perform(post("/api/buyers/carts/{cartId}/items", emptyCartId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId": "%s", "quantity": 0, "unitPrice": 14500000, "currency": "COP"}
                                """.formatted(macbookId)))
                .andExpect(status().isBadRequest());

        // Verificar que la orden original pagada y entregada sigue intacta en la base de datos
        mockMvc.perform(get("/api/buyers/orders/{orderId}", orderId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("DELIVERED_COMPLETED"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    private String extractJsonField(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("Field '" + fieldName + "' not found in JSON: " + json);
    }
}

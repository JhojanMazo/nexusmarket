package application.adapters.useCases;

import application.adapters.aspect.Auditable;
import application.adapters.security.SecurityUserContext;
import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import application.domain.enums.AuditSeverity;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.SellerPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.ValidateSellerOwnershipService;
import application.domain.services.user.UniqueIdentityValidationService;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.PageResult;
import application.domain.valueobjects.ProductVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SellerUseCaseImpl implements SellerPort {

    private final UserRepositoryPort userRepository;
    private final ProductRepositoryPort productRepository;
    private final OrderRepositoryPort orderRepository;
    private final WarehouseRepositoryPort warehouseRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final UniqueIdentityValidationService uniqueIdentityValidationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    @Autowired
    public SellerUseCaseImpl(UserRepositoryPort userRepository,
                             ProductRepositoryPort productRepository,
                             OrderRepositoryPort orderRepository,
                             WarehouseRepositoryPort warehouseRepository,
                             InventoryRepositoryPort inventoryRepository,
                             UniqueIdentityValidationService uniqueIdentityValidationService,
                             ValidateSellerOwnershipService validateSellerOwnershipService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.uniqueIdentityValidationService = uniqueIdentityValidationService;
        this.validateSellerOwnershipService = validateSellerOwnershipService;
    }

    public SellerUseCaseImpl(UserRepositoryPort userRepository,
                             ProductRepositoryPort productRepository,
                             OrderRepositoryPort orderRepository,
                             UniqueIdentityValidationService uniqueIdentityValidationService) {
        this(userRepository, productRepository, orderRepository, null, null,
                uniqueIdentityValidationService, new ValidateSellerOwnershipService());
    }

    @Override
    @Transactional
    public Seller registerSeller(UUID adminUserId, String fullName, String email, String docType, String docNumber, LocalDate onboardingDate) {
        uniqueIdentityValidationService.validateUniqueIdentity(userRepository, email, docType, docNumber);

        Seller seller = Seller.create(
                adminUserId,
                fullName,
                new Email(email),
                new IdentityDocument(docType, docNumber),
                onboardingDate != null ? onboardingDate : LocalDate.now()
        );
        User saved = userRepository.save(seller);
        return (Seller) saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Seller getSellerProfile(UUID sellerId) {
        User user = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor", sellerId));
        if (!(user instanceof Seller seller)) {
            throw new IllegalArgumentException("El usuario con ID " + sellerId + " no es un vendedor");
        }
        return seller;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products_catalog", allEntries = true),
            @CacheEvict(value = "seller_products", allEntries = true)
    })
    @Auditable(action = "PRODUCT_CREATED", resource = "Product", severity = AuditSeverity.MEDIUM)
    public Product createProduct(String name, String variantAttribute, String variantValue,
                                 BigDecimal price, String currencyCode, ProductStatus status, ProductType type) {
        UUID authenticatedSellerId = SecurityUserContext.get()
                .map(AuthenticatedUserPrincipal::userId)
                .orElse(null);
        return createProduct(authenticatedSellerId, name, variantAttribute, variantValue, price, currencyCode, status, type);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products_catalog", allEntries = true),
            @CacheEvict(value = "seller_products", allEntries = true)
    })
    @Auditable(action = "PRODUCT_CREATED", resource = "Product", severity = AuditSeverity.MEDIUM)
    public Product createProduct(UUID sellerId, String name, String variantAttribute, String variantValue,
                                 BigDecimal price, String currencyCode, ProductStatus status, ProductType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (price == null || status == null || type == null) {
            throw new IllegalArgumentException("Precio, estado y tipo son obligatorios.");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("La moneda es obligatoria.");
        }

        assertAuthenticatedSellerCanOperate(sellerId);

        UUID effectiveSellerId = sellerId != null
                ? sellerId
                : SecurityUserContext.get().map(AuthenticatedUserPrincipal::userId).orElse(null);

        String normalizedName = name.trim();
        if (normalizedName.length() > 150) {
            throw new IllegalArgumentException("El nombre no puede superar 150 caracteres.");
        }

        Currency currency = parseCurrency(currencyCode);

        Product product = new Product(
                UUID.randomUUID(),
                effectiveSellerId,
                normalizedName,
                new ProductVariant(variantAttribute, variantValue),
                new Money(price, currency),
                status,
                type
        );

        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "seller_products", key = "#page + '-' + #size")
    public PageResult<Product> getAllProducts(int page, int size) {
        return productRepository.findAll(page, size, "name", "ASC");
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "seller_products", key = "#page + '-' + #size + '-' + #sortBy + '-' + #direction")
    public PageResult<Product> getAllProducts(int page, int size, String sortBy, String direction) {
        return productRepository.findAll(page, size, sortBy, direction);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products_catalog", allEntries = true),
            @CacheEvict(value = "seller_products", allEntries = true)
    })
    @Auditable(action = "PRODUCT_STATUS_UPDATED", resource = "Product", severity = AuditSeverity.MEDIUM)
    public Product updateProductStatus(UUID productId, ProductStatus status) {
        UUID currentSellerId = SecurityUserContext.get()
                .map(AuthenticatedUserPrincipal::userId)
                .orElse(null);
        return updateProductStatus(currentSellerId, productId, status);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products_catalog", allEntries = true),
            @CacheEvict(value = "seller_products", allEntries = true)
    })
    @Auditable(action = "PRODUCT_STATUS_UPDATED", resource = "Product", severity = AuditSeverity.MEDIUM)
    public Product updateProductStatus(UUID sellerId, UUID productId, ProductStatus status) {
        Product existing = getProductById(productId);
        if (sellerId != null && existing.getSellerId() != null) {
            validateSellerOwnershipService.validateOwnership(sellerId, existing.getSellerId());
        }
        Product updated = existing.withStatus(status);
        return productRepository.save(updated);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products_catalog", allEntries = true),
            @CacheEvict(value = "seller_products", allEntries = true)
    })
    @Auditable(action = "PRODUCT_PRICE_UPDATED", resource = "Product", severity = AuditSeverity.HIGH)
    public Product updateProductPrice(UUID productId, BigDecimal price, String currencyCode) {
        UUID currentSellerId = SecurityUserContext.get()
                .map(AuthenticatedUserPrincipal::userId)
                .orElse(null);
        return updateProductPrice(currentSellerId, productId, price, currencyCode);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products_catalog", allEntries = true),
            @CacheEvict(value = "seller_products", allEntries = true)
    })
    @Auditable(action = "PRODUCT_PRICE_UPDATED", resource = "Product", severity = AuditSeverity.HIGH)
    public Product updateProductPrice(UUID sellerId, UUID productId, BigDecimal price, String currencyCode) {
        Product existing = getProductById(productId);
        if (sellerId != null && existing.getSellerId() != null) {
            validateSellerOwnershipService.validateOwnership(sellerId, existing.getSellerId());
        }
        Currency currency = (currencyCode != null && !currencyCode.isBlank())
                ? parseCurrency(currencyCode)
                : existing.getPrice().currency();
        Product updated = existing.withPrice(new Money(price, currency));
        return productRepository.save(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getSellerOrders(UUID sellerId) {
        if (sellerId == null) {
            return Collections.emptyList();
        }
        assertAuthenticatedSellerCanOperate(sellerId);
        return orderRepository.findAll().stream()
                .filter(order -> order.getItems().stream()
                        .anyMatch(item -> item.product() != null && sellerId.equals(item.product().getSellerId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Order> getSellerOrders(UUID sellerId, int page, int size, String sortBy, String direction) {
        List<Order> filtered = getSellerOrders(sellerId);
        return PageResult.of(filtered, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Warehouse> getSellerWarehouses(UUID sellerId) {
        assertAuthenticatedSellerCanOperate(sellerId);
        getSellerProfile(sellerId);
        return warehouseRepository.findBySellerId(sellerId);
    }

    @Override
    @Transactional
    @Auditable(action = "INVENTORY_INITIAL_STOCK_REGISTERED", resource = "Inventory", severity = AuditSeverity.MEDIUM)
    public Inventory registerInitialStock(UUID sellerId, UUID productId, UUID warehouseId, int quantity) {
        assertAuthenticatedSellerCanOperate(sellerId);
        getSellerProfile(sellerId);
        Product product = getProductById(productId);
        if (product.getSellerId() != null) {
            validateSellerOwnershipService.validateOwnership(sellerId, product.getSellerId());
        }
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Bodega", warehouseId));
        if (warehouse.getSellerId() != null && !sellerId.equals(warehouse.getSellerId())) {
            throw new UnauthorizedOperationException("La bodega no pertenece al vendedor");
        }

        var existing = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId);
        Inventory inventory;
        if (existing.isPresent()) {
            inventory = existing.get();
            inventory.registerEntry(quantity, "Entrada de stock inicial por vendedor");
        } else {
            inventory = Inventory.create(product, warehouse, quantity);
        }
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    @Auditable(action = "INVENTORY_STOCK_ADJUSTED", resource = "Inventory", severity = AuditSeverity.HIGH)
    public Inventory adjustStock(UUID sellerId, UUID inventoryId, int newQuantity, String reason) {
        assertAuthenticatedSellerCanOperate(sellerId);
        getSellerProfile(sellerId);
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario", inventoryId));
        if (inventory.getWarehouse().getSellerId() != null && !sellerId.equals(inventory.getWarehouse().getSellerId())) {
            throw new UnauthorizedOperationException("El inventario no pertenece al vendedor");
        }
        if (inventory.getProduct() != null && inventory.getProduct().getSellerId() != null) {
            validateSellerOwnershipService.validateOwnership(sellerId, inventory.getProduct().getSellerId());
        }
        inventory.adjustStock(newQuantity, reason);
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> getSellerInventory(UUID sellerId) {
        assertAuthenticatedSellerCanOperate(sellerId);
        getSellerProfile(sellerId);
        List<Warehouse> warehouses = warehouseRepository.findBySellerId(sellerId);
        return warehouses.stream()
                .flatMap(w -> inventoryRepository.findByWarehouseId(w.getWarehouseId()).stream())
                .toList();
    }

    private void assertAuthenticatedSellerCanOperate(UUID requestedSellerId) {
        if (requestedSellerId == null) {
            return;
        }
        SecurityUserContext.get().ifPresent(principal -> {
            if ("SELLER".equalsIgnoreCase(principal.role()) && !principal.userId().equals(requestedSellerId)) {
                throw new UnauthorizedOperationException("No está autorizado para operar con los recursos de otro vendedor");
            }
        });
    }

    private Currency parseCurrency(String currencyCode) {
        try {
            return Currency.getInstance(currencyCode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Código de moneda inválido. Ejemplo: COP o USD.");
        }
    }
}

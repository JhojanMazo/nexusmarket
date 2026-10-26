package application.adapters.useCases;

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
import application.domain.valueobjects.ProductVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
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
    public Seller getSellerProfile(UUID sellerId) {
        User user = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor", sellerId));
        if (!(user instanceof Seller seller)) {
            throw new IllegalArgumentException("El usuario con ID " + sellerId + " no es un vendedor");
        }
        return seller;
    }

    @Override
    public Product createProduct(String name, String variantAttribute, String variantValue,
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

        String normalizedName = name.trim();
        if (normalizedName.length() > 150) {
            throw new IllegalArgumentException("El nombre no puede superar 150 caracteres.");
        }

        Currency currency = parseCurrency(currencyCode);

        Product product = new Product(
                UUID.randomUUID(),
                normalizedName,
                new ProductVariant(variantAttribute, variantValue),
                new Money(price, currency),
                status,
                type
        );

        return productRepository.save(product);
    }

    @Override
    public Product getProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", productId));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product updateProductStatus(UUID productId, ProductStatus status) {
        Product existing = getProductById(productId);
        Product updated = existing.withStatus(status);
        return productRepository.save(updated);
    }

    @Override
    public Product updateProductPrice(UUID productId, BigDecimal price, String currencyCode) {
        Product existing = getProductById(productId);
        Currency currency = (currencyCode != null && !currencyCode.isBlank())
                ? parseCurrency(currencyCode)
                : existing.getPrice().currency();
        Product updated = existing.withPrice(new Money(price, currency));
        return productRepository.save(updated);
    }

    @Override
    public List<Order> getSellerOrders(UUID sellerId) {
        return orderRepository.findAll();
    }

    @Override
    public List<Warehouse> getSellerWarehouses(UUID sellerId) {
        getSellerProfile(sellerId);
        return warehouseRepository.findBySellerId(sellerId);
    }

    @Override
    public Inventory registerInitialStock(UUID sellerId, UUID productId, UUID warehouseId, int quantity) {
        getSellerProfile(sellerId);
        Product product = getProductById(productId);
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
    public Inventory adjustStock(UUID sellerId, UUID inventoryId, int newQuantity, String reason) {
        getSellerProfile(sellerId);
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario", inventoryId));
        if (inventory.getWarehouse().getSellerId() != null && !sellerId.equals(inventory.getWarehouse().getSellerId())) {
            throw new UnauthorizedOperationException("El inventario no pertenece al vendedor");
        }
        inventory.adjustStock(newQuantity, reason);
        return inventoryRepository.save(inventory);
    }

    @Override
    public List<Inventory> getSellerInventory(UUID sellerId) {
        getSellerProfile(sellerId);
        List<Warehouse> warehouses = warehouseRepository.findBySellerId(sellerId);
        return warehouses.stream()
                .flatMap(w -> inventoryRepository.findByWarehouseId(w.getWarehouseId()).stream())
                .toList();
    }

    private Currency parseCurrency(String currencyCode) {
        try {
            return Currency.getInstance(currencyCode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Código de moneda inválido. Ejemplo: COP o USD.");
        }
    }
}

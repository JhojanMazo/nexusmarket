package application.domain.ports.in;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.Warehouse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SellerPort {
    Seller registerSeller(UUID adminUserId, String fullName, String email, String docType, String docNumber, LocalDate onboardingDate);
    Seller getSellerProfile(UUID sellerId);

    Product createProduct(String name, String variantAttribute, String variantValue,
                          BigDecimal price, String currencyCode, ProductStatus status, ProductType type);
    Product getProductById(UUID productId);
    List<Product> getAllProducts();
    Product updateProductStatus(UUID productId, ProductStatus status);
    Product updateProductPrice(UUID productId, BigDecimal price, String currencyCode);

    List<Order> getSellerOrders(UUID sellerId);

    List<Warehouse> getSellerWarehouses(UUID sellerId);
    Inventory registerInitialStock(UUID sellerId, UUID productId, UUID warehouseId, int quantity);
    Inventory adjustStock(UUID sellerId, UUID inventoryId, int newQuantity, String reason);
    List<Inventory> getSellerInventory(UUID sellerId);
}

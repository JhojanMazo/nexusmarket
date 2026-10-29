package application.domain.ports.in;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.valueobjects.PageResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SellerPort {
    Seller registerSeller(UUID adminUserId, String fullName, String email, String docType, String docNumber, LocalDate onboardingDate);
    Seller getSellerProfile(UUID sellerId);

    Product createProduct(String name, String variantAttribute, String variantValue,
                          BigDecimal price, String currencyCode, ProductStatus status, ProductType type);
    default Product createProduct(UUID sellerId, String name, String variantAttribute, String variantValue,
                                  BigDecimal price, String currencyCode, ProductStatus status, ProductType type) {
        return createProduct(name, variantAttribute, variantValue, price, currencyCode, status, type);
    }
    Product getProductById(UUID productId);
    List<Product> getAllProducts();
    default PageResult<Product> getAllProducts(int page, int size, String sortBy, String direction) {
        return PageResult.of(getAllProducts(), page, size);
    }
    default PageResult<Product> getAllProducts(int page, int size) {
        return getAllProducts(page, size, "name", "ASC");
    }
    Product updateProductStatus(UUID productId, ProductStatus status);
    default Product updateProductStatus(UUID sellerId, UUID productId, ProductStatus status) {
        return updateProductStatus(productId, status);
    }
    Product updateProductPrice(UUID productId, BigDecimal price, String currencyCode);
    default Product updateProductPrice(UUID sellerId, UUID productId, BigDecimal price, String currencyCode) {
        return updateProductPrice(productId, price, currencyCode);
    }

    List<Order> getSellerOrders(UUID sellerId);
    default PageResult<Order> getSellerOrders(UUID sellerId, int page, int size, String sortBy, String direction) {
        return PageResult.of(getSellerOrders(sellerId), page, size);
    }
    default PageResult<Order> getSellerOrders(UUID sellerId, int page, int size) {
        return getSellerOrders(sellerId, page, size, "orderDate", "DESC");
    }

    List<Warehouse> getSellerWarehouses(UUID sellerId);
    Inventory registerInitialStock(UUID sellerId, UUID productId, UUID warehouseId, int quantity);
    Inventory adjustStock(UUID sellerId, UUID inventoryId, int newQuantity, String reason);
    List<Inventory> getSellerInventory(UUID sellerId);
}

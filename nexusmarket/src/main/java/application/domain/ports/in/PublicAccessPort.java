package application.domain.ports.in;

import application.domain.models.AuthenticationResult;
import application.domain.models.Buyer;
import application.domain.models.Product;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.PageResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PublicAccessPort {
    AuthenticationResult authenticate(String email, String password);
    void logout(String token);
    Buyer registerBuyer(String fullName, String email, String docType, String docNumber, Address primaryAddress);
    default Buyer registerBuyer(String fullName, String email, String docType, String docNumber, Address primaryAddress, String rawPassword) {
        return registerBuyer(fullName, email, docType, docNumber, primaryAddress);
    }
    List<Product> getPublishedCatalog();
    default PageResult<Product> getPublishedCatalog(int page, int size) {
        return PageResult.of(getPublishedCatalog(), page, size);
    }
    default PageResult<Product> getPublishedCatalog(int page, int size, String sortBy, String direction) {
        return getPublishedCatalog(page, size);
    }
    default PageResult<Product> searchPublishedCatalog(String search, String type, BigDecimal minPrice, BigDecimal maxPrice,
                                                       int page, int size, String sortBy, String direction) {
        return getPublishedCatalog(page, size, sortBy, direction);
    }
    Product getPublishedProductById(UUID productId);
    default int getAvailableStockForProduct(UUID productId) {
        return 0;
    }
}

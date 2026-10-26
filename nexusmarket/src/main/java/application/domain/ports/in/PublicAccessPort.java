package application.domain.ports.in;

import application.domain.models.AuthenticationResult;
import application.domain.models.Buyer;
import application.domain.models.Product;
import application.domain.valueobjects.Address;

import java.util.List;
import java.util.UUID;

public interface PublicAccessPort {
    AuthenticationResult authenticate(String email, String password);
    void logout(String token);
    Buyer registerBuyer(String fullName, String email, String docType, String docNumber, Address primaryAddress);
    List<Product> getPublishedCatalog();
    Product getPublishedProductById(UUID productId);
}

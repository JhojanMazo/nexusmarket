package application.domain.ports.out;

import application.domain.models.User;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for User aggregates (Staff, Buyer, Seller).
 */
public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UUID userId);
    Optional<User> findByEmail(Email email);
    Optional<User> findByIdentityDocument(IdentityDocument identityDocument);
    List<User> findAll();
}

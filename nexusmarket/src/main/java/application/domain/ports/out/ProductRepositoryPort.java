package application.domain.ports.out;

import application.domain.models.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for Product aggregates.
 */
public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findById(UUID productId);
    List<Product> findAll();
}

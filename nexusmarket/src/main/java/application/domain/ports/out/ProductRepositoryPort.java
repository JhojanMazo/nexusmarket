package application.domain.ports.out;

import application.domain.enums.ProductStatus;
import application.domain.models.Product;
import application.domain.valueobjects.PageResult;

import java.util.Comparator;
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

    default PageResult<Product> findAll(int page, int size, String sortBy, String direction) {
        List<Product> all = sortProducts(findAll(), sortBy, direction);
        return PageResult.of(all, page, size);
    }

    default PageResult<Product> findAll(int page, int size) {
        return findAll(page, size, "name", "ASC");
    }

    default PageResult<Product> findByStatus(ProductStatus status, int page, int size, String sortBy, String direction) {
        List<Product> filtered = findAll().stream()
                .filter(p -> p.getStatus() == status)
                .toList();
        return PageResult.of(sortProducts(filtered, sortBy, direction), page, size);
    }

    private static List<Product> sortProducts(List<Product> products, String sortBy, String direction) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }
        Comparator<Product> comparator;
        if ("price".equalsIgnoreCase(sortBy) || "priceAmount".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(p -> p.getPrice().amount());
        } else if ("status".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(p -> p.getStatus().name());
        } else if ("productId".equalsIgnoreCase(sortBy) || "id".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Product::getProductId);
        } else {
            comparator = Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
        }
        if ("DESC".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }
        return products.stream().sorted(comparator).toList();
    }
}

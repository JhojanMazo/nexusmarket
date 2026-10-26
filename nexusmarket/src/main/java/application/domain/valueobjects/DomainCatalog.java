package application.domain.valueobjects;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value object representing a domain catalog classification or category taxonomy.
 */
public record DomainCatalog(
        String code,
        String category,
        String description,
        boolean active
) implements Serializable {

    public DomainCatalog {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Catalog code cannot be empty");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Catalog category cannot be empty");
        }
    }

    public static DomainCatalog of(String code, String category, String description) {
        return new DomainCatalog(code, category, description, true);
    }
}

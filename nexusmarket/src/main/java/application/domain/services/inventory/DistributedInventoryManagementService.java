package application.domain.services.inventory;

import application.domain.models.Inventory;
import application.domain.models.Product;
import application.domain.models.Warehouse;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

/**
 * 4.2 Links catalog items with physical storage facilities.
 */
@Service
public class DistributedInventoryManagementService {

    public Inventory linkInventory(Product product, Warehouse warehouse, int initialStock) {
        Objects.requireNonNull(product, "Product must not be null");
        Objects.requireNonNull(warehouse, "Warehouse must not be null");
        return new Inventory(UUID.randomUUID(), product, warehouse, initialStock);
    }
}

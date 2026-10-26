package application.domain.services.inventory;

import application.domain.models.Inventory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 5.1 Handles physical stock reconciliations and return integrations (FR-014).
 */
@Service
public class InventoryAdjustmentsReturnsService {

    public void processReturn(Inventory inventory, int quantity) {
        Objects.requireNonNull(inventory, "Inventory must not be null");
        inventory.restockFromReturn(quantity);
    }

    public void adjustStock(Inventory inventory, int realQuantity, String reason) {
        Objects.requireNonNull(inventory, "Inventory must not be null");
        inventory.adjustStock(realQuantity, reason);
    }
}

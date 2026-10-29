package application.domain.services.inventory;

import application.domain.models.Inventory;
import java.util.Objects;

/**
 * 4.3 Records inbound stock additions.
 */
public class InventoryEntryRegistrationService {

    public void registerEntry(Inventory inventory, int quantity, String reference) {
        Objects.requireNonNull(inventory, "Inventory must not be null");
        inventory.registerEntry(quantity, reference);
    }
}

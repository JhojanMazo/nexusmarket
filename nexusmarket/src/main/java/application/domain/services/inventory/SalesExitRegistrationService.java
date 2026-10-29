package application.domain.services.inventory;

import application.domain.enums.InventoryStatus;
import application.domain.models.Inventory;
import java.util.Objects;

/**
 * 4.5 Commits stock reductions upon confirmed purchases (FR-010, FR-011).
 */
public class SalesExitRegistrationService {

    public void registerExit(Inventory inventory, int quantity) {
        Objects.requireNonNull(inventory, "Inventory must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad de salida debe ser mayor a cero");
        }
        if (inventory.getStatus() == InventoryStatus.DAMAGED) {
            throw new IllegalStateException("No se puede registrar salida de inventario sobre un lote marcado como DAMAGED (FR-011)");
        }
        if (inventory.getReservedQuantity() - quantity < 0) {
            throw new IllegalStateException("Bajo ninguna circunstancia se permitirán existencias negativas (FR-010)");
        }
        inventory.confirmSaleOutbound(quantity);
    }
}

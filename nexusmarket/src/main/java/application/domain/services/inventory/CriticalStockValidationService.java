package application.domain.services.inventory;

import application.domain.enums.InventoryStatus;
import application.domain.models.Inventory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 5.2 Prevents negative inventory and protects critical thresholds (FR-010, FR-011, FR-012).
 * Absolute business rule: Under no circumstances are negative stock balances permitted.
 */
@Service
public class CriticalStockValidationService {

    public boolean validateStock(Inventory inventory, int requestedQuantity) {
        Objects.requireNonNull(inventory, "Inventory must not be null");
        if (requestedQuantity <= 0) {
            throw new IllegalArgumentException("La cantidad solicitada debe ser mayor a cero");
        }
        if (inventory.getAvailableQuantity() < 0 || inventory.getReservedQuantity() < 0) {
            throw new IllegalStateException("Bajo ninguna circunstancia se permitirán existencias negativas (FR-010)");
        }
        return inventory.getStatus() != InventoryStatus.DAMAGED &&
               inventory.getAvailableQuantity() >= requestedQuantity &&
               (inventory.getAvailableQuantity() - requestedQuantity) >= 0;
    }

    public void assertValidActiveLotAndNonNegativeBalance(Inventory inventory, int quantityToDeduct) {
        Objects.requireNonNull(inventory, "Inventory must not be null");
        if (quantityToDeduct <= 0) {
            throw new IllegalArgumentException("La cantidad solicitada debe ser mayor a cero");
        }
        if (inventory.getStatus() == InventoryStatus.DAMAGED) {
            throw new IllegalStateException("El lote asociado en bodega está marcado como DAMAGED y no puede operar (FR-011)");
        }
        if (inventory.getAvailableQuantity() < quantityToDeduct || (inventory.getAvailableQuantity() - quantityToDeduct) < 0) {
            throw new IllegalStateException("Bajo ninguna circunstancia se permitirán existencias negativas (FR-010, FR-012)");
        }
    }
}

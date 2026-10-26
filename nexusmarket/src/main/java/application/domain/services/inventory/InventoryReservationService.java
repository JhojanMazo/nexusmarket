package application.domain.services.inventory;

import application.domain.enums.InventoryStatus;
import application.domain.models.Inventory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 4.4 Temporarily locks stock during checkout workflows and commits outbound exits (FR-010, FR-011, FR-012, FR-013).
 * Absolute business rule: Under no circumstances are negative stock balances permitted, nor operations on DAMAGED lots.
 */
@Service
public class InventoryReservationService {

    public void reserve(Inventory inventory, int quantity) {
        Objects.requireNonNull(inventory, "Inventory must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad a reservar debe ser mayor a cero");
        }
        if (inventory.getStatus() == InventoryStatus.DAMAGED) {
            throw new IllegalStateException("No se puede reservar inventario con estado DAMAGED (FR-011)");
        }
        if (inventory.getAvailableQuantity() - quantity < 0) {
            throw new IllegalStateException("Bajo ninguna circunstancia se permitirán existencias negativas (FR-010, FR-012)");
        }
        inventory.reserve(quantity);
    }

    public void registerSaleExit(Inventory inventory, int quantity) {
        Objects.requireNonNull(inventory, "Inventory must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad de salida debe ser mayor a cero");
        }
        if (inventory.getStatus() == InventoryStatus.DAMAGED) {
            throw new IllegalStateException("No se puede registrar una salida sobre un lote marcado como DAMAGED (FR-011)");
        }
        if (inventory.getReservedQuantity() - quantity < 0) {
            throw new IllegalStateException("Bajo ninguna circunstancia se permitirán existencias negativas en salida de inventario (FR-010)");
        }
        inventory.confirmSaleOutbound(quantity);
    }
}

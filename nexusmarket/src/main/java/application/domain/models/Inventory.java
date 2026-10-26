package application.domain.models;

import application.domain.enums.InventoryMovementType;
import application.domain.enums.InventoryStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root for warehouse product stock.
 * Enforces business rules FR-010 to FR-015.
 */
public class Inventory {
    private final UUID inventoryId;
    private final Product product;
    private final Warehouse warehouse;
    private int availableQuantity;
    private int reservedQuantity;
    private InventoryStatus status;
    private final List<InventoryMovement> movements = new ArrayList<>();

    public Inventory(UUID inventoryId, Product product, Warehouse warehouse, int initialStock) {
        this.inventoryId = Objects.requireNonNull(inventoryId);
        this.product = Objects.requireNonNull(product);
        this.warehouse = Objects.requireNonNull(warehouse);
        if (initialStock < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo");
        }
        this.availableQuantity = initialStock;
        this.reservedQuantity = 0;
        this.status = InventoryStatus.AVAILABLE;
        if (initialStock > 0) {
            recordMovement(InventoryMovementType.INBOUND, initialStock, "Stock inicial");
        }
    }

    public static Inventory create(Product product, Warehouse warehouse, int initialStock) {
        return new Inventory(UUID.randomUUID(), product, warehouse, initialStock);
    }

    public static Inventory reconstitute(UUID inventoryId, Product product, Warehouse warehouse,
                                         int availableQuantity, int reservedQuantity,
                                         InventoryStatus status, List<InventoryMovement> movements) {
        if (availableQuantity < 0 || reservedQuantity < 0) {
            throw new IllegalArgumentException("Bajo ninguna circunstancia se permitirán existencias negativas (FR-010)");
        }
        Inventory inventory = new Inventory(inventoryId, product, warehouse, 0);
        inventory.availableQuantity = availableQuantity;
        inventory.reservedQuantity = reservedQuantity;
        inventory.status = status;
        inventory.movements.clear();
        if (movements != null) {
            inventory.movements.addAll(movements);
        }
        return inventory;
    }

    public UUID getInventoryId() { return inventoryId; }
    public Product getProduct() { return product; }
    public Warehouse getWarehouse() { return warehouse; }
    public int getAvailableQuantity() { return availableQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public InventoryStatus getStatus() { return status; }
    public void setStatus(InventoryStatus status) { this.status = Objects.requireNonNull(status); }
    public List<InventoryMovement> getMovements() { return Collections.unmodifiableList(movements); }

    public void registerEntry(int quantity, String reference) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad de entrada debe ser mayor a cero");
        }
        this.availableQuantity += quantity;
        recordMovement(InventoryMovementType.INBOUND, quantity, reference);
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad a reservar debe ser mayor a cero");
        }
        if (status == InventoryStatus.DAMAGED) {
            throw new IllegalStateException("No se puede reservar inventario con estado DAMAGED (FR-011)");
        }
        if (quantity > availableQuantity || (this.availableQuantity - quantity) < 0) {
            throw new IllegalStateException("Stock insuficiente: solicitado " + quantity + ", disponible " + availableQuantity + ". Bajo ninguna circunstancia se permiten existencias negativas (FR-010, FR-012)");
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
        recordMovement(InventoryMovementType.RESERVATION, quantity, "Reserva de orden");
    }

    public void confirmSaleOutbound(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (status == InventoryStatus.DAMAGED) {
            throw new IllegalStateException("No se puede registrar salida de inventario con estado DAMAGED (FR-011)");
        }
        if (quantity > reservedQuantity || (this.reservedQuantity - quantity) < 0) {
            throw new IllegalStateException("No se puede confirmar salida por cantidad superior a la reservada. Bajo ninguna circunstancia se permiten existencias negativas (FR-010)");
        }
        this.reservedQuantity -= quantity;
        recordMovement(InventoryMovementType.SALE_OUTBOUND, quantity, "Venta confirmada");
    }

    public void restockFromReturn(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad a retornar debe ser mayor a cero");
        }
        this.availableQuantity += quantity;
        recordMovement(InventoryMovementType.RETURN, quantity, "Retorno de orden");
    }

    public void adjustStock(int realQuantity, String reason) {
        if (realQuantity < 0) {
            throw new IllegalArgumentException("La cantidad real no puede ser negativa (FR-010)");
        }
        int diff = realQuantity - this.availableQuantity;
        if (diff != 0) {
            this.availableQuantity = realQuantity;
            recordMovement(InventoryMovementType.ADJUSTMENT, diff, reason != null ? reason : "Ajuste manual de stock");
        }
    }

    private void recordMovement(InventoryMovementType type, int quantity, String reference) {
        InventoryMovement movement = new InventoryMovement(UUID.randomUUID(), this.inventoryId, type, quantity, Instant.now(), reference);
        movements.add(movement);
    }
}

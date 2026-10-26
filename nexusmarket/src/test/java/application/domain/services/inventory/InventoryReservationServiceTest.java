package application.domain.services.inventory;

import application.domain.services.user.*;
import application.domain.services.product.*;
import application.domain.services.order.*;
import application.domain.services.billing.*;
import application.domain.services.inventory.*;
import application.domain.services.logistics.*;
import application.domain.services.authorization.*;

import application.domain.enums.InventoryStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.WarehouseType;
import application.domain.models.Inventory;
import application.domain.models.Product;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InventoryReservationServiceTest {

    private InventoryReservationService service;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        service = new InventoryReservationService();

        Product product = new Product(
                UUID.randomUUID(),
                "Laptop Pro 16",
                new ProductVariant("RAM", "32GB"),
                new Money(new BigDecimal("1800.00"), Currency.getInstance("USD")),
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );

        Warehouse warehouse = new Warehouse(
                UUID.randomUUID(),
                "Bodega Central",
                new Address("Zona Franca", "Bogota", "Cundinamarca", "Colombia", "110111", null),
                WarehouseType.MARKETPLACE,
                null,
                5000.0
        );

        inventory = Inventory.create(product, warehouse, 10);
    }

    @Test
    void reserve_Success() {
        service.reserve(inventory, 4);

        assertEquals(6, inventory.getAvailableQuantity());
        assertEquals(4, inventory.getReservedQuantity());
    }

    @Test
    void reserve_ExceedsAvailableStock_ThrowsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> service.reserve(inventory, 11));
    }

    @Test
    void reserve_DamagedStatus_ThrowsIllegalStateException() {
        inventory.setStatus(InventoryStatus.DAMAGED);
        assertThrows(IllegalStateException.class, () -> service.reserve(inventory, 2));
    }

    @Test
    void reserve_ZeroOrNegativeQuantity_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.reserve(inventory, 0));
        assertThrows(IllegalArgumentException.class, () -> service.reserve(inventory, -3));
    }

    @Test
    void registerSaleExit_SuccessAndPreventsNegativeOrDamagedStock() {
        service.reserve(inventory, 5);
        service.registerSaleExit(inventory, 3);
        assertEquals(2, inventory.getReservedQuantity());

        assertThrows(IllegalStateException.class, () -> service.registerSaleExit(inventory, 5));

        inventory.setStatus(InventoryStatus.DAMAGED);
        assertThrows(IllegalStateException.class, () -> service.registerSaleExit(inventory, 1));
    }
}

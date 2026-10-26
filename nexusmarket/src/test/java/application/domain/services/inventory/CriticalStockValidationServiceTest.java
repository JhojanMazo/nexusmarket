package application.domain.services.inventory;

import application.domain.enums.InventoryStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.WarehouseType;
import application.domain.models.Inventory;
import application.domain.models.Product;
import application.domain.models.Warehouse;
import application.domain.services.inventory.CriticalStockValidationService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CriticalStockValidationServiceTest {

    private CriticalStockValidationService service;
    private Product product;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        service = new CriticalStockValidationService();
        product = new Product(UUID.randomUUID(), "Prod", new ProductVariant("V", "V"),
                new Money(BigDecimal.TEN, Currency.getInstance("USD")), ProductStatus.PUBLISHED, ProductType.PHYSICAL);
        warehouse = new Warehouse(UUID.randomUUID(), "Main WH", new Address("St", "City", "Dep", "CO", "111", null), WarehouseType.MARKETPLACE, null);
    }

    @Test
    void shouldReturnTrueWhenSufficientStockAvailable() {
        Inventory inv = new Inventory(UUID.randomUUID(), product, warehouse, 50);
        assertTrue(service.validateStock(inv, 10));
        assertTrue(service.validateStock(inv, 50));
    }

    @Test
    void shouldReturnFalseWhenInsufficientStock() {
        Inventory inv = new Inventory(UUID.randomUUID(), product, warehouse, 10);
        assertFalse(service.validateStock(inv, 15));
    }

    @Test
    void shouldReturnFalseWhenInventoryIsDamaged() {
        Inventory inv = new Inventory(UUID.randomUUID(), product, warehouse, 100);
        inv.setStatus(InventoryStatus.DAMAGED);
        assertFalse(service.validateStock(inv, 5));
    }

    @Test
    void shouldThrowWhenRequestedQuantityIsZeroOrNegative() {
        Inventory inv = new Inventory(UUID.randomUUID(), product, warehouse, 50);
        assertThrows(IllegalArgumentException.class, () -> service.validateStock(inv, 0));
        assertThrows(IllegalArgumentException.class, () -> service.validateStock(inv, -3));
    }

    @Test
    void assertValidActiveLotAndNonNegativeBalance_RejectsDamagedOrNegativeBalance() {
        Inventory inv = new Inventory(UUID.randomUUID(), product, warehouse, 5);
        assertDoesNotThrow(() -> service.assertValidActiveLotAndNonNegativeBalance(inv, 5));
        assertThrows(IllegalStateException.class, () -> service.assertValidActiveLotAndNonNegativeBalance(inv, 6));

        inv.setStatus(InventoryStatus.DAMAGED);
        assertThrows(IllegalStateException.class, () -> service.assertValidActiveLotAndNonNegativeBalance(inv, 1));
    }
}

package application.domain.services.inventory;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.WarehouseType;
import application.domain.models.Inventory;
import application.domain.models.Product;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductVariant;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public class InventoryServiceHarness {

    private final InventoryReservationService reservationService;
    private final CriticalStockValidationService stockValidationService;

    public InventoryServiceHarness(InventoryReservationService reservationService,
                                   CriticalStockValidationService stockValidationService) {
        this.reservationService = reservationService;
        this.stockValidationService = stockValidationService;
    }

    public static Inventory createInventoryFixture(int availableQuantity, int minStock, int maxStock) {
        Warehouse warehouse = new Warehouse(
                UUID.randomUUID(),
                "Central Hub",
                new Address("Calle 50", "Medellin", "Antioquia", "Colombia", "05001", null),
                WarehouseType.MARKETPLACE,
                null
        );
        Product product = new Product(
                UUID.randomUUID(),
                "Stocked Item",
                new ProductVariant("V", "1"),
                new Money(BigDecimal.TEN, Currency.getInstance("USD")),
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );
        return new Inventory(
                UUID.randomUUID(),
                product,
                warehouse,
                availableQuantity
        );
    }

    public InventoryReservationService getReservationService() {
        return reservationService;
    }

    public CriticalStockValidationService getStockValidationService() {
        return stockValidationService;
    }
}

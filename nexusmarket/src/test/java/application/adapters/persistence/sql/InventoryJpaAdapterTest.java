package application.adapters.persistence.sql;

import application.adapters.persistence.jpa.InventoryJpaAdapter;
import application.adapters.persistence.jpa.entities.InventoryJpaEntity;
import application.adapters.persistence.jpa.mappers.InventoryJpaMapper;
import application.adapters.persistence.jpa.mappers.InventoryMovementJpaMapper;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.mappers.WarehouseJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInventoryRepository;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryJpaAdapterTest {

    @Mock
    private SpringDataJpaInventoryRepository jpaRepository;

    private InventoryJpaMapper mapper;
    private InventoryJpaAdapter adapter;

    private Inventory inventory;
    private InventoryJpaEntity entity;
    private UUID inventoryId;
    private UUID productId;
    private UUID warehouseId;

    @BeforeEach
    void setUp() {
        mapper = new InventoryJpaMapper(new ProductJpaMapper(), new WarehouseJpaMapper(), new InventoryMovementJpaMapper());
        adapter = new InventoryJpaAdapter(jpaRepository, mapper);

        productId = UUID.randomUUID();
        Product product = new Product(
                productId, "Test Product",
                new ProductVariant("Size", "M"),
                new Money(BigDecimal.TEN, Currency.getInstance("USD")),
                ProductStatus.PUBLISHED, ProductType.PHYSICAL
        );

        warehouseId = UUID.randomUUID();
        Address address = new Address("Street 1", "City", "State", "Country", "12345", null);
        Warehouse warehouse = new Warehouse(warehouseId, "Warehouse Test", address, WarehouseType.MARKETPLACE, null, 500.0);

        inventoryId = UUID.randomUUID();
        inventory = new Inventory(inventoryId, product, warehouse, 100);
        entity = mapper.toEntity(inventory);
    }

    @Test
    void save_Success() {
        when(jpaRepository.save(any(InventoryJpaEntity.class))).thenReturn(entity);

        Inventory saved = adapter.save(inventory);

        assertNotNull(saved);
        assertEquals(inventoryId, saved.getInventoryId());
        assertEquals(100, saved.getAvailableQuantity());
        verify(jpaRepository, times(1)).save(any(InventoryJpaEntity.class));
    }

    @Test
    void findById_Success() {
        when(jpaRepository.findById(inventoryId)).thenReturn(Optional.of(entity));

        Optional<Inventory> found = adapter.findById(inventoryId);

        assertTrue(found.isPresent());
        assertEquals(100, found.get().getAvailableQuantity());
    }

    @Test
    void findByProductIdAndWarehouseId_Success() {
        when(jpaRepository.findByProductProductIdAndWarehouseWarehouseId(productId, warehouseId))
                .thenReturn(Optional.of(entity));

        Optional<Inventory> found = adapter.findByProductIdAndWarehouseId(productId, warehouseId);

        assertTrue(found.isPresent());
        assertEquals(inventoryId, found.get().getInventoryId());
    }

    @Test
    void findByWarehouseId_Success() {
        when(jpaRepository.findByWarehouseWarehouseId(warehouseId)).thenReturn(List.of(entity));

        List<Inventory> list = adapter.findByWarehouseId(warehouseId);

        assertEquals(1, list.size());
    }
}

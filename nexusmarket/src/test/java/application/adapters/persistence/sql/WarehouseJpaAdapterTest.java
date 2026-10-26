package application.adapters.persistence.sql;

import application.adapters.persistence.jpa.WarehouseJpaAdapter;
import application.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import application.adapters.persistence.jpa.mappers.WarehouseJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaWarehouseRepository;
import application.domain.enums.WarehouseType;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseJpaAdapterTest {

    @Mock
    private SpringDataJpaWarehouseRepository jpaRepository;

    private WarehouseJpaMapper mapper;
    private WarehouseJpaAdapter adapter;

    private Warehouse warehouse;
    private WarehouseJpaEntity entity;
    private UUID warehouseId;

    @BeforeEach
    void setUp() {
        mapper = new WarehouseJpaMapper();
        adapter = new WarehouseJpaAdapter(jpaRepository, mapper);

        warehouseId = UUID.randomUUID();
        Address address = new Address("Carrera 7", "Bogota", "Cundinamarca", "Colombia", "110111", "Piso 5");
        warehouse = new Warehouse(warehouseId, "Bodega Norte", address, WarehouseType.MARKETPLACE, null, 1000.0);
        entity = mapper.toEntity(warehouse);
    }

    @Test
    void save_Success() {
        when(jpaRepository.save(any(WarehouseJpaEntity.class))).thenReturn(entity);

        Warehouse saved = adapter.save(warehouse);

        assertNotNull(saved);
        assertEquals(warehouseId, saved.getWarehouseId());
        assertEquals("Bodega Norte", saved.getName());
        verify(jpaRepository, times(1)).save(any(WarehouseJpaEntity.class));
    }

    @Test
    void findById_Success() {
        when(jpaRepository.findById(warehouseId)).thenReturn(Optional.of(entity));

        Optional<Warehouse> found = adapter.findById(warehouseId);

        assertTrue(found.isPresent());
        assertEquals("Bodega Norte", found.get().getName());
    }

    @Test
    void findByType_Success() {
        when(jpaRepository.findByType(WarehouseType.MARKETPLACE)).thenReturn(List.of(entity));

        List<Warehouse> list = adapter.findByType(WarehouseType.MARKETPLACE);

        assertEquals(1, list.size());
        assertEquals(WarehouseType.MARKETPLACE, list.get(0).getType());
    }
}

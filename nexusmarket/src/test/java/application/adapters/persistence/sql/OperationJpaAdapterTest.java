package application.adapters.persistence.sql;
import application.adapters.persistence.jpa.*;

import application.adapters.persistence.jpa.entities.OperationJpaEntity;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOperationRepository;
import application.domain.models.Operation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationJpaAdapterTest {

    @Mock
    private SpringDataJpaOperationRepository repository;

    private OperationJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OperationJpaAdapter(repository);
    }

    @Test
    void shouldSaveOperation() {
        UUID opId = UUID.randomUUID();
        UUID operatorId = UUID.randomUUID();
        Operation domain = new Operation(opId, "ORDER_CREATED", "Order placed", operatorId, "COMPLETED", "{}", Instant.now());
        OperationJpaEntity entity = new OperationJpaEntity(opId, "ORDER_CREATED", "Order placed", operatorId, "COMPLETED", "{}", Instant.now());

        when(repository.save(any(OperationJpaEntity.class))).thenReturn(entity);

        Operation result = adapter.save(domain);
        assertNotNull(result);
        assertEquals(opId, result.getId());
        assertEquals("ORDER_CREATED", result.getOperationType());
        verify(repository).save(any(OperationJpaEntity.class));
    }

    @Test
    void shouldFindById() {
        UUID opId = UUID.randomUUID();
        OperationJpaEntity entity = new OperationJpaEntity(opId, "ORDER_CREATED", "Order placed", UUID.randomUUID(), "COMPLETED", "{}", Instant.now());

        when(repository.findById(opId)).thenReturn(Optional.of(entity));

        Optional<Operation> result = adapter.findById(opId);
        assertTrue(result.isPresent());
        assertEquals(opId, result.get().getId());
    }

    @Test
    void shouldFindAll() {
        OperationJpaEntity entity = new OperationJpaEntity(UUID.randomUUID(), "ORDER_CREATED", "Order placed", UUID.randomUUID(), "COMPLETED", "{}", Instant.now());
        when(repository.findAll()).thenReturn(List.of(entity));

        List<Operation> result = adapter.findAll();
        assertEquals(1, result.size());
    }
}

package application.domain.ports.out;

import application.domain.models.Operation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for Operation records.
 */
public interface OperationRepositoryPort {
    Operation save(Operation operation);
    Optional<Operation> findById(UUID operationId);
    List<Operation> findAll();
}

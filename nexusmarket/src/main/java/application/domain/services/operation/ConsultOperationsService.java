package application.domain.services.operation;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Operation;
import application.domain.ports.out.OperationRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Domain service to query operations.
 */
@Service
public class ConsultOperationsService {

    private final OperationRepositoryPort operationRepositoryPort;

    public ConsultOperationsService(OperationRepositoryPort operationRepositoryPort) {
        this.operationRepositoryPort = operationRepositoryPort;
    }

    public Operation findById(UUID operationId) {
        return operationRepositoryPort.findById(operationId)
                .orElseThrow(() -> new EntityNotFoundException("Operation", operationId));
    }

    public List<Operation> findAll() {
        return operationRepositoryPort.findAll();
    }
}

package application.domain.services.operation;

import application.domain.models.Operation;
import application.domain.ports.out.OperationRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Domain service to register business operations.
 */
@Service
public class RegisterOperationService {

    private final OperationRepositoryPort operationRepositoryPort;

    public RegisterOperationService(OperationRepositoryPort operationRepositoryPort) {
        this.operationRepositoryPort = operationRepositoryPort;
    }

    public Operation execute(String operationType, String description, UUID operatorId, String payload) {
        Operation operation = Operation.create(operationType, description, operatorId, payload);
        return operationRepositoryPort.save(operation);
    }
}

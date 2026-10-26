package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.OperationJpaEntity;
import application.adapters.persistence.jpa.mappers.OperationJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOperationRepository;
import application.domain.models.Operation;
import application.domain.ports.out.OperationRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class OperationJpaAdapter implements OperationRepositoryPort {

    private final SpringDataJpaOperationRepository repository;

    public OperationJpaAdapter(SpringDataJpaOperationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Operation save(Operation operation) {
        OperationJpaEntity entity = OperationJpaMapper.toEntity(operation);
        OperationJpaEntity saved = repository.save(entity);
        return OperationJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Operation> findById(UUID operationId) {
        return repository.findById(operationId).map(OperationJpaMapper::toDomain);
    }

    @Override
    public List<Operation> findAll() {
        return repository.findAll().stream()
                .map(OperationJpaMapper::toDomain)
                .collect(Collectors.toList());
    }
}

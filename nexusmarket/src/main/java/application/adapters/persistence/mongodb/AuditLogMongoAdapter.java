package application.adapters.persistence.mongodb;

import application.adapters.persistence.mongodb.documents.AuditLogDocument;
import application.adapters.persistence.mongodb.mappers.AuditLogMongoMapper;
import application.adapters.persistence.mongodb.repositories.AuditLogMongoRepository;
import application.domain.models.AuditLog;
import application.domain.ports.out.AuditLogRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AuditLogMongoAdapter implements AuditLogRepositoryPort {

    private final AuditLogMongoRepository repository;

    public AuditLogMongoAdapter(AuditLogMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogDocument document = AuditLogMongoMapper.toDocument(auditLog);
        AuditLogDocument saved = repository.save(document);
        return AuditLogMongoMapper.toDomain(saved);
    }

    @Override
    public Optional<AuditLog> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id.toString()).map(AuditLogMongoMapper::toDomain);
    }

    @Override
    public List<AuditLog> findByUserId(UUID userId) {
        if (userId == null) {
            return List.of();
        }
        return repository.findByUserId(userId.toString()).stream()
                .map(AuditLogMongoMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findAll() {
        return repository.findAll().stream()
                .map(AuditLogMongoMapper::toDomain)
                .collect(Collectors.toList());
    }
}

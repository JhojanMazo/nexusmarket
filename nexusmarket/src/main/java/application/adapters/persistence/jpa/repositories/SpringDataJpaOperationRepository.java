package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.OperationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataJpaOperationRepository extends JpaRepository<OperationJpaEntity, UUID> {
}

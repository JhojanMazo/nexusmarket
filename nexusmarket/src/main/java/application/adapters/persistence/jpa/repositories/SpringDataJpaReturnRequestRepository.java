package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.ReturnRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataJpaReturnRequestRepository extends JpaRepository<ReturnRequestJpaEntity, UUID> {
    Optional<ReturnRequestJpaEntity> findByOrderOrderId(UUID orderId);
}

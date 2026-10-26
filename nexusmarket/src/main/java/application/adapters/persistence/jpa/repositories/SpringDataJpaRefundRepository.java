package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.RefundJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataJpaRefundRepository extends JpaRepository<RefundJpaEntity, UUID> {
    Optional<RefundJpaEntity> findByReturnRequestRequestId(UUID returnRequestId);
}

package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataJpaOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {
    List<OrderJpaEntity> findByBuyer_UserId(UUID buyerId);
    Page<OrderJpaEntity> findAll(Pageable pageable);
    Page<OrderJpaEntity> findByBuyer_UserId(UUID buyerId, Pageable pageable);
}

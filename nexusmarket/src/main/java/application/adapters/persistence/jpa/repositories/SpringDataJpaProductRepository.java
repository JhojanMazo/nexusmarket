package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.domain.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataJpaProductRepository extends JpaRepository<ProductJpaEntity, UUID> {
    Page<ProductJpaEntity> findAll(Pageable pageable);
    Page<ProductJpaEntity> findByStatus(ProductStatus status, Pageable pageable);
}

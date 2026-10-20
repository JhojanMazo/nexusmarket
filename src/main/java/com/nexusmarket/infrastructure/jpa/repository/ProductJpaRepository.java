package com.nexusmarket.infrastructure.jpa.repository;

import com.nexusmarket.infrastructure.jpa.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {
}

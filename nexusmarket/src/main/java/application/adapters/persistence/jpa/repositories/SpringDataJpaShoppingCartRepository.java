package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.ShoppingCartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataJpaShoppingCartRepository extends JpaRepository<ShoppingCartJpaEntity, UUID> {
    Optional<ShoppingCartJpaEntity> findByBuyer_UserId(UUID buyerId);
}

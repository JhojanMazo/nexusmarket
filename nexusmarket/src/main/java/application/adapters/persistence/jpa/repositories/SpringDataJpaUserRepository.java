package application.adapters.persistence.jpa.repositories;

import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.adapters.persistence.jpa.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataJpaUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(Email email);
    Optional<UserEntity> findByIdentityDocument(IdentityDocument identityDocument);
}

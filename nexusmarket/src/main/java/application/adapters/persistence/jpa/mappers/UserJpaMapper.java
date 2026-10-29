package application.adapters.persistence.jpa.mappers;

import application.domain.enums.UserRole;
import application.domain.models.Buyer;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.adapters.persistence.jpa.entities.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserJpaMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) return null;

        User domainUser;
        if (entity instanceof BuyerEntity buyerEntity) {
            List<Address> additional = buyerEntity.getSecondaryAddresses() != null
                    ? buyerEntity.getSecondaryAddresses().stream().map(AddressEmbeddable::toDomain).toList()
                    : Collections.emptyList();
            domainUser = Buyer.reconstitute(
                    buyerEntity.getUserId(),
                    buyerEntity.getFullName(),
                    buyerEntity.getEmail(),
                    buyerEntity.getIdentityDocument(),
                    buyerEntity.getPrimaryAddress() != null ? buyerEntity.getPrimaryAddress().toDomain() : null,
                    additional,
                    buyerEntity.getCommercialStatus(),
                    buyerEntity.getStatus()
            );
        } else if (entity instanceof SellerEntity sellerEntity) {
            java.time.LocalDate onboardingDate = sellerEntity.getOnboardingDate() != null
                    ? sellerEntity.getOnboardingDate()
                    : (sellerEntity.getCreatedAt() != null ? sellerEntity.getCreatedAt().toLocalDate() : java.time.LocalDate.now());
            domainUser = Seller.reconstitute(
                    sellerEntity.getUserId(),
                    sellerEntity.getFullName(),
                    sellerEntity.getEmail(),
                    sellerEntity.getIdentityDocument(),
                    onboardingDate,
                    sellerEntity.getStatus()
            );
        } else {
            domainUser = User.reconstituteStaff(
                    entity.getUserId(),
                    entity.getFullName(),
                    entity.getEmail(),
                    entity.getIdentityDocument(),
                    entity.getRole(),
                    entity.getStatus()
            );
        }
        domainUser.setPasswordHash(entity.getPasswordHash());
        return domainUser;
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) return null;

        UserEntity entity;
        if (domain instanceof Buyer buyer) {
            List<AddressEmbeddable> secondary = buyer.getAdditionalAddresses() != null
                    ? buyer.getAdditionalAddresses().stream().map(AddressEmbeddable::fromDomain).toList()
                    : Collections.emptyList();
            entity = new BuyerEntity(
                    buyer.getUserId(),
                    buyer.getFullName(),
                    buyer.getEmail(),
                    buyer.getIdentityDocument(),
                    buyer.getStatus(),
                    AddressEmbeddable.fromDomain(buyer.getPrimaryAddress()),
                    secondary,
                    buyer.getCommercialStatus()
            );
        } else if (domain instanceof Seller seller) {
            entity = new SellerEntity(
                    seller.getUserId(),
                    seller.getFullName(),
                    seller.getEmail(),
                    seller.getIdentityDocument(),
                    seller.getStatus(),
                    seller.getOnboardingDate()
            );
        } else if (domain.getRole() == UserRole.ADMINISTRATOR) {
            entity = new AdministratorEntity(
                    domain.getUserId(),
                    domain.getFullName(),
                    domain.getEmail(),
                    domain.getIdentityDocument(),
                    domain.getStatus()
            );
        } else if (domain.getRole() == UserRole.LOGISTICS_OPERATOR) {
            entity = new LogisticsOperatorEntity(
                    domain.getUserId(),
                    domain.getFullName(),
                    domain.getEmail(),
                    domain.getIdentityDocument(),
                    domain.getStatus()
            );
        } else if (domain.getRole() == UserRole.SUPERVISOR) {
            entity = new SupervisorEntity(
                    domain.getUserId(),
                    domain.getFullName(),
                    domain.getEmail(),
                    domain.getIdentityDocument(),
                    domain.getStatus()
            );
        } else {
            throw new IllegalArgumentException("Rol de usuario no soportado para persistencia: " + domain.getRole());
        }
        entity.setPasswordHash(domain.getPasswordHash());
        return entity;
    }
}

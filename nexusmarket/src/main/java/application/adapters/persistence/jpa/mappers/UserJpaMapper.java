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

        if (entity instanceof BuyerEntity buyerEntity) {
            List<Address> additional = buyerEntity.getSecondaryAddresses() != null
                    ? buyerEntity.getSecondaryAddresses().stream().map(AddressEmbeddable::toDomain).toList()
                    : Collections.emptyList();
            return Buyer.reconstitute(
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
            return Seller.reconstitute(
                    sellerEntity.getUserId(),
                    sellerEntity.getFullName(),
                    sellerEntity.getEmail(),
                    sellerEntity.getIdentityDocument(),
                    sellerEntity.getOnboardingDate(),
                    sellerEntity.getStatus()
            );
        } else {
            return User.reconstituteStaff(
                    entity.getUserId(),
                    entity.getFullName(),
                    entity.getEmail(),
                    entity.getIdentityDocument(),
                    entity.getRole(),
                    entity.getStatus()
            );
        }
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) return null;

        if (domain instanceof Buyer buyer) {
            List<AddressEmbeddable> secondary = buyer.getAdditionalAddresses() != null
                    ? buyer.getAdditionalAddresses().stream().map(AddressEmbeddable::fromDomain).toList()
                    : Collections.emptyList();
            return new BuyerEntity(
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
            return new SellerEntity(
                    seller.getUserId(),
                    seller.getFullName(),
                    seller.getEmail(),
                    seller.getIdentityDocument(),
                    seller.getStatus(),
                    seller.getOnboardingDate()
            );
        } else if (domain.getRole() == UserRole.ADMINISTRATOR) {
            return new AdministratorEntity(
                    domain.getUserId(),
                    domain.getFullName(),
                    domain.getEmail(),
                    domain.getIdentityDocument(),
                    domain.getStatus()
            );
        } else if (domain.getRole() == UserRole.LOGISTICS_OPERATOR) {
            return new LogisticsOperatorEntity(
                    domain.getUserId(),
                    domain.getFullName(),
                    domain.getEmail(),
                    domain.getIdentityDocument(),
                    domain.getStatus()
            );
        } else if (domain.getRole() == UserRole.SUPERVISOR) {
            return new SupervisorEntity(
                    domain.getUserId(),
                    domain.getFullName(),
                    domain.getEmail(),
                    domain.getIdentityDocument(),
                    domain.getStatus()
            );
        } else {
            throw new IllegalArgumentException("Rol de usuario no soportado para persistencia: " + domain.getRole());
        }
    }
}

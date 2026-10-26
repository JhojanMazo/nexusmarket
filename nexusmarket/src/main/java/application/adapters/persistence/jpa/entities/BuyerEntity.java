package application.adapters.persistence.jpa.entities;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA representation of a Buyer user.
 */
@Entity(name = "BuyerEntity")
@DiscriminatorValue("BUYER")
@Getter
@Setter
@NoArgsConstructor
public class BuyerEntity extends UserEntity {

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "primary_street")),
            @AttributeOverride(name = "city", column = @Column(name = "primary_city")),
            @AttributeOverride(name = "stateOrProvince", column = @Column(name = "primary_state_or_province")),
            @AttributeOverride(name = "country", column = @Column(name = "primary_country")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "primary_postal_code")),
            @AttributeOverride(name = "reference", column = @Column(name = "primary_reference"))
    })
    private AddressEmbeddable primaryAddress;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "buyer_secondary_addresses", joinColumns = @JoinColumn(name = "user_id"))
    private List<AddressEmbeddable> secondaryAddresses = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "commercial_status")
    private BuyerCommercialStatus commercialStatus;

    public BuyerEntity(UUID userId, String fullName, Email email, IdentityDocument identityDocument,
                       UserStatus status, AddressEmbeddable primaryAddress, BuyerCommercialStatus commercialStatus) {
        super(userId, fullName, email, identityDocument, UserRole.BUYER, status);
        this.primaryAddress = primaryAddress;
        this.commercialStatus = commercialStatus;
    }

    public BuyerEntity(UUID userId, String fullName, Email email, IdentityDocument identityDocument,
                       UserStatus status, AddressEmbeddable primaryAddress, List<AddressEmbeddable> secondaryAddresses,
                       BuyerCommercialStatus commercialStatus) {
        super(userId, fullName, email, identityDocument, UserRole.BUYER, status);
        this.primaryAddress = primaryAddress;
        if (secondaryAddresses != null) {
            this.secondaryAddresses.addAll(secondaryAddresses);
        }
        this.commercialStatus = commercialStatus;
    }
}

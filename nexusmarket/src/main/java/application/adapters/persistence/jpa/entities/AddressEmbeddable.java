package application.adapters.persistence.jpa.entities;

import application.domain.valueobjects.Address;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressEmbeddable {

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state_or_province", nullable = false)
    private String stateOrProvince;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "reference")
    private String reference;

    public static AddressEmbeddable fromDomain(Address address) {
        if (address == null) return null;
        return new AddressEmbeddable(
                address.street(),
                address.city(),
                address.stateOrProvince(),
                address.country(),
                address.postalCode(),
                address.reference()
        );
    }

    public Address toDomain() {
        return new Address(street, city, stateOrProvince, country, postalCode, reference);
    }
}

package application.domain.models;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a Buyer.
 *
 * FR‑003: A Buyer can purchase only when status is ACTIVE and commercial status is ENABLED.
 * This class stores the primary address, optional additional addresses, and commercial status.
 */
public class Buyer extends User {
    private Address primaryAddress;
    private final List<Address> additionalAddresses;
    private BuyerCommercialStatus commercialStatus;

    private Buyer(UUID userId,
                  String fullName,
                  Email email,
                  IdentityDocument identityDocument,
                  Address primaryAddress,
                  List<Address> additionalAddresses,
                  BuyerCommercialStatus commercialStatus) {
        super(userId, fullName, email, identityDocument, UserRole.BUYER, UserStatus.ACTIVE);
        this.primaryAddress = Objects.requireNonNull(primaryAddress);
        this.additionalAddresses = new ArrayList<>(
                additionalAddresses != null ? additionalAddresses : Collections.emptyList());
        this.commercialStatus = Objects.requireNonNull(commercialStatus);
    }

    /**
     * Factory method for creating a Buyer.
     */
    public static Buyer create(String fullName,
                               Email email,
                               IdentityDocument idDoc,
                               Address primaryAddress,
                               List<Address> additionalAddresses,
                               BuyerCommercialStatus commercialStatus) {
        return new Buyer(UUID.randomUUID(), fullName, email, idDoc, primaryAddress, additionalAddresses, commercialStatus);
    }

    public static Buyer create(String fullName,
                               Email email,
                               IdentityDocument idDoc,
                               Address primaryAddress) {
        return create(fullName, email, idDoc, primaryAddress, Collections.emptyList(), BuyerCommercialStatus.ENABLED);
    }

    public static Buyer reconstitute(UUID userId,
                                      String fullName,
                                      Email email,
                                      IdentityDocument idDoc,
                                      Address primaryAddress,
                                      List<Address> additionalAddresses,
                                      BuyerCommercialStatus commercialStatus,
                                      UserStatus status) {
        Buyer buyer = new Buyer(userId, fullName, email, idDoc, primaryAddress, additionalAddresses, commercialStatus);
        buyer.setStatus(status);
        return buyer;
    }

    public Address getPrimaryAddress() {
        return primaryAddress;
    }

    public void updatePrimaryAddress(Address primaryAddress) {
        this.primaryAddress = Objects.requireNonNull(primaryAddress, "La dirección principal es obligatoria");
    }

    public List<Address> getAdditionalAddresses() {
        return Collections.unmodifiableList(additionalAddresses);
    }

    public List<Address> getSecondaryAddresses() {
        return Collections.unmodifiableList(additionalAddresses);
    }

    public void addAdditionalAddress(Address address) {
        additionalAddresses.add(Objects.requireNonNull(address, "La dirección adicional no puede ser nula"));
    }

    public void updateAdditionalAddress(int index, Address newAddress) {
        Objects.requireNonNull(newAddress, "La nueva dirección adicional no puede ser nula");
        if (index < 0 || index >= additionalAddresses.size()) {
            throw new IllegalArgumentException("Índice de dirección adicional fuera de rango: " + index);
        }
        additionalAddresses.set(index, newAddress);
    }

    public void removeAdditionalAddress(int index) {
        if (index < 0 || index >= additionalAddresses.size()) {
            throw new IllegalArgumentException("Índice de dirección adicional fuera de rango: " + index);
        }
        additionalAddresses.remove(index);
    }

    public void removeAdditionalAddress(Address address) {
        Objects.requireNonNull(address, "La dirección a eliminar no puede ser nula");
        additionalAddresses.remove(address);
    }

    public void replaceAdditionalAddresses(List<Address> addresses) {
        additionalAddresses.clear();
        if (addresses != null) {
            for (Address addr : addresses) {
                additionalAddresses.add(Objects.requireNonNull(addr));
            }
        }
    }

    public BuyerCommercialStatus getCommercialStatus() {
        return commercialStatus;
    }

    public void setCommercialStatus(BuyerCommercialStatus commercialStatus) {
        this.commercialStatus = Objects.requireNonNull(commercialStatus);
    }
}

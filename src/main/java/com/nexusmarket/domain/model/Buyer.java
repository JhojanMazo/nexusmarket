package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.BuyerCommercialStatus;
import com.nexusmarket.domain.enums.UserRole;
import com.nexusmarket.domain.enums.UserStatus;
import com.nexusmarket.domain.valueobject.Address;
import com.nexusmarket.domain.valueobject.Email;
import com.nexusmarket.domain.valueobject.IdentityDocument;
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
    private final Address primaryAddress;
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

    public Address getPrimaryAddress() {
        return primaryAddress;
    }

    public List<Address> getAdditionalAddresses() {
        return Collections.unmodifiableList(additionalAddresses);
    }

    public void addAdditionalAddress(Address address) {
        additionalAddresses.add(Objects.requireNonNull(address));
    }

    public BuyerCommercialStatus getCommercialStatus() {
        return commercialStatus;
    }

    public void setCommercialStatus(BuyerCommercialStatus commercialStatus) {
        this.commercialStatus = Objects.requireNonNull(commercialStatus);
    }
}

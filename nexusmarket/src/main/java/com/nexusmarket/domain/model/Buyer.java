package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.BuyerCommercialStatus;
import com.nexusmarket.domain.enums.UserRole;
import com.nexusmarket.domain.enums.UserStatus;
import com.nexusmarket.domain.valueobject.Address;
import com.nexusmarket.domain.valueobject.Email;
import com.nexusmarket.domain.valueobject.IdentityDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Specialization of User with role BUYER.
 * Kept as a dedicated subclass because it carries state and behavior beyond
 * User: a required primary address, optional additional addresses, and a
 * commercial status that gates its ability to purchase.
 * Business rule: a buyer never manages other buyers' information or
 * inventory; that restriction is enforced by the application layer, not by
 * this entity.
 */
public class Buyer extends User {

    private Address primaryAddress;
    private final List<Address> additionalAddresses = new ArrayList<>();
    private BuyerCommercialStatus commercialStatus;

    public Buyer(String userId, String fullName, Email email, IdentityDocument identityDocument,
                 UserStatus status, Address primaryAddress, BuyerCommercialStatus commercialStatus) {
        super(userId, fullName, email, identityDocument, UserRole.BUYER, status);
        this.primaryAddress = Objects.requireNonNull(primaryAddress, "primaryAddress is required");
        this.commercialStatus = Objects.requireNonNull(commercialStatus, "commercialStatus is required");
    }

    public boolean canPurchase() {
        return isActive() && commercialStatus == BuyerCommercialStatus.ENABLED;
    }

    public void addAdditionalAddress(Address address) {
        additionalAddresses.add(Objects.requireNonNull(address, "address is required"));
    }

    public Address getPrimaryAddress() { return primaryAddress; }
    public void setPrimaryAddress(Address primaryAddress) {
        this.primaryAddress = Objects.requireNonNull(primaryAddress, "primaryAddress is required");
    }

    public List<Address> getAdditionalAddresses() { return List.copyOf(additionalAddresses); }

    public BuyerCommercialStatus getCommercialStatus() { return commercialStatus; }
    public void setCommercialStatus(BuyerCommercialStatus commercialStatus) {
        this.commercialStatus = Objects.requireNonNull(commercialStatus, "commercialStatus is required");
    }
}

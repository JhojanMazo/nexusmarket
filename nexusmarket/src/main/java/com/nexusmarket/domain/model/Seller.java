package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.UserRole;
import com.nexusmarket.domain.enums.UserStatus;
import com.nexusmarket.domain.valueobject.Email;
import com.nexusmarket.domain.valueobject.IdentityDocument;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Specialization of User with role SELLER.
 * Kept as a dedicated subclass because it carries state beyond User: a trade
 * name, an onboarding date, and the list of warehouses it operates.
 * Business rule: sellers cannot self-register; they are onboarded by an
 * Administrator (see {@link com.nexusmarket.domain.service.SellerOnboardingService}).
 */
public class Seller extends User {

    private String tradeName;
    private final LocalDate onboardingDate;
    private final List<Warehouse> associatedWarehouses = new ArrayList<>();

    public Seller(String userId, String fullName, Email email, IdentityDocument identityDocument,
                  UserStatus status, String tradeName, LocalDate onboardingDate) {
        super(userId, fullName, email, identityDocument, UserRole.SELLER, status);
        this.tradeName = requireNotBlank(tradeName, "tradeName");
        this.onboardingDate = Objects.requireNonNull(onboardingDate, "onboardingDate is required");
    }

    /**
     * Links a warehouse to this seller. Exposed as public for persistence/
     * reconstruction purposes, but new links driven by the onboarding process
     * should go through {@link com.nexusmarket.domain.service.SellerOnboardingService}
     * so the "who may onboard a seller" rule stays enforced in one place.
     */
    public void linkWarehouse(Warehouse warehouse) {
        associatedWarehouses.add(Objects.requireNonNull(warehouse, "warehouse is required"));
    }

    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) {
        this.tradeName = requireNotBlank(tradeName, "tradeName");
    }

    public LocalDate getOnboardingDate() { return onboardingDate; }

    public List<Warehouse> getAssociatedWarehouses() { return List.copyOf(associatedWarehouses); }
}

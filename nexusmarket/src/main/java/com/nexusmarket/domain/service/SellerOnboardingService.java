package com.nexusmarket.domain.service;

import com.nexusmarket.domain.enums.UserRole;
import com.nexusmarket.domain.model.Seller;
import com.nexusmarket.domain.model.User;
import com.nexusmarket.domain.model.Warehouse;

public final class SellerOnboardingService {

    private SellerOnboardingService() {
    }

    public static void onboard(User actor, Seller seller, Warehouse firstWarehouse) {
        if (actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new IllegalStateException("Only a user with the ADMINISTRATOR role can onboard a new seller.");
        }
        seller.linkWarehouse(firstWarehouse);
    }
}

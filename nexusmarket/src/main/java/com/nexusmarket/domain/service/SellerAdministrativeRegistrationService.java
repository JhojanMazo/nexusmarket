package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.User;
import com.nexusmarket.domain.model.Seller;

public class SellerAdministrativeRegistrationService {

    public Seller registerSeller(com.nexusmarket.domain.model.User admin, Seller newSeller) {
        if (!admin.hasPermission("REGISTER_SELLER")) {
            throw new SecurityException("Unauthorized: Admin permission required to register sellers.");
        }
        newSeller.setStatus(com.nexusmarket.domain.enums.UserStatus.PENDING_APPROVAL);
        return newSeller;
    }
}
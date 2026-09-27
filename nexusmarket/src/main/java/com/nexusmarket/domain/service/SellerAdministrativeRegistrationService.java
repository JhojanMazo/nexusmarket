package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Admin;
import com.nexusmarket.domain.entity.Seller;

public class SellerAdministrativeRegistrationService {

    public Seller registerSeller(Admin admin, Seller newSeller) {
        if (!admin.hasPermission("REGISTER_SELLER")) {
            throw new SecurityException("Unauthorized: Admin permission required to register sellers.");
        }
        newSeller.setStatus("PENDING_APPROVAL");
        return newSeller;
    }
}
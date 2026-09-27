package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Address;
import com.nexusmarket.domain.entity.Buyer;

public class PrimaryAddressManagementService {

    public void setPrimaryAddress(Buyer buyer, Address address) {
        if (address == null || !address.isValid()) {
            throw new IllegalArgumentException("Invalid primary address provided.");
        }
        buyer.setPrimaryAddress(address);
    }
}
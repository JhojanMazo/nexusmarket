package com.nexusmarket.domain.service;

import com.nexusmarket.domain.valueobject.Address;
import com.nexusmarket.domain.model.Buyer;

public class PrimaryAddressManagementService {

    public void setPrimaryAddress(Buyer buyer, Address address) {
        if (address == null || !address.isValid()) {
            throw new IllegalArgumentException("Invalid primary address provided.");
        }
        buyer.setPrimaryAddress(address);
    }
}
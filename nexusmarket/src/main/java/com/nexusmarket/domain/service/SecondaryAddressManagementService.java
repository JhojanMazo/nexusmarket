package com.nexusmarket.domain.service;

import com.nexusmarket.domain.valueobject.Address;
import com.nexusmarket.domain.model.Buyer;

public class SecondaryAddressManagementService {

    public void addSecondaryAddress(Buyer buyer, Address address) {
        if (buyer == null || address == null) {
            throw new IllegalArgumentException("Buyer and address are required.");
        }
        buyer.getSecondaryAddresses().add(address);
    }
}
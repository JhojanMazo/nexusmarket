package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Buyer;

public class BuyerManagementService {

    public void updateCommercialInfo(Buyer buyer, String commercialData) {
        if (buyer == null) {
            throw new IllegalArgumentException("Buyer cannot be null.");
        }
        buyer.setCommercialData(commercialData);
    }
}
package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Buyer;

public class BuyerCommercialStateControlService {

    public void verifyAndEnableTransactions(Buyer buyer) {
        if (buyer.hasValidPaymentMethod() && buyer.hasPrimaryAddress()) {
            buyer.setCanTransact(true);
        } else {
            buyer.setCanTransact(false);
        }
    }
}
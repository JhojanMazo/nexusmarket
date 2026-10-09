package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Inventory;

public class SalesExitRegistrationService {

    public void registerExit(Inventory inventory, int quantity) {
        inventory.confirmSaleOutbound(quantity);
    }
}
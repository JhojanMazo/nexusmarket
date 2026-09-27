package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Inventory;

public class SalesExitRegistrationService {

    public void registerExit(Inventory inventory, int quantity) {
        inventory.commitReservation(quantity);
    }
}
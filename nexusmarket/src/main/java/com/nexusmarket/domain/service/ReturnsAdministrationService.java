package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Order;

public class ReturnsAdministrationService {

    public void initiateReturn(Order order) {
        if (!"DELIVERED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Only delivered orders can be submitted for return.");
        }
        order.setStatus("RETURN_IN_PROGRESS");
    }
}
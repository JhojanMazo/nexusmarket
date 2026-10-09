package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Order;

public class ReturnsAdministrationService {

    public void initiateReturn(Order order) {
        if (order.getStatus() != com.nexusmarket.domain.enums.OrderStatus.DELIVERED_COMPLETED) {
            throw new IllegalStateException("Only delivered orders can be submitted for return.");
        }
        order.setStatus(com.nexusmarket.domain.enums.OrderStatus.RETURN_IN_PROGRESS);
    }
}
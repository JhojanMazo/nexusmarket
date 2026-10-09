package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Order;

public class RefundsManagementService {

    public void processRefund(Order order) {
        if (order.getStatus() != com.nexusmarket.domain.enums.OrderStatus.RETURNED) {
            throw new IllegalStateException("Order must be in RETURNED status before processing refund.");
        }
        order.setStatus(com.nexusmarket.domain.enums.OrderStatus.REFUNDED);
    }
}
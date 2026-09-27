package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Order;

public class RefundsManagementService {

    public void processRefund(Order order) {
        if (!"RETURNED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Order must be in RETURNED status before processing refund.");
        }
        order.setStatus("REFUNDED");
    }
}
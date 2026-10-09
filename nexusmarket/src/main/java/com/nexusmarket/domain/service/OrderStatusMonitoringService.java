package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Order;

public class OrderStatusMonitoringService {

    public void updateStatus(Order order, String newStatus) {
        if (order == null || newStatus == null) {
            throw new IllegalArgumentException("Order and new status are required.");
        }
        order.setStatus(newStatus);
    }
}
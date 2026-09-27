package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Order;

public class CompletedOrderImmutabilityService {

    public void ensureImmutability(Order order) {
        if ("COMPLETED".equalsIgnoreCase(order.getStatus())) {
            order.lock(); // Locks the order against any further modifications
        }
    }
}
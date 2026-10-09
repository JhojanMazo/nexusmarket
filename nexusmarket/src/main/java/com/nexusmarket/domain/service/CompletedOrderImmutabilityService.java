package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Order;

public class CompletedOrderImmutabilityService {

    public void ensureImmutability(Order order) {
        if (order.getStatus() == com.nexusmarket.domain.enums.OrderStatus.COMPLETED) {
            order.lock(); // Locks the order against any further modifications
        }
    }
}
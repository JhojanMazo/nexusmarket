package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Order;

public class LogisticsShippingManagementService {

    public void coordinateShipping(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }
        order.setStatus(com.nexusmarket.domain.enums.OrderStatus.SHIPPED);
    }
}
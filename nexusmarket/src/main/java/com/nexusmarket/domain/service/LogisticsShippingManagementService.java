package com.nexusmarket.domain.service;

import com.nexusmarket.domain.entity.Order;

public class LogisticsShippingManagementService {

    public void coordinateShipping(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }
        order.setStatus("DISPATCHED");
    }
}
package com.nexusmarket.domain.repository;

import com.nexusmarket.domain.model.Order;
public interface OrderRepository {
    Order save(Order order);
    Order findById(String id);
}

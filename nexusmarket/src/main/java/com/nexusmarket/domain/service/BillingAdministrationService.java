package com.nexusmarket.domain.service;

import com.nexusmarket.domain.model.Invoice;
import com.nexusmarket.domain.model.Order;

public class BillingAdministrationService {

    public Invoice generateInvoice(Order order) {
        if (!order.isPaid()) {
            throw new IllegalStateException("Cannot generate invoice: Order has not been paid.");
        }
        return new Invoice(order);
    }
}
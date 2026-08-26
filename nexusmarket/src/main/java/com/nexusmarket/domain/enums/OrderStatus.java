package com.nexusmarket.domain.enums;

/**
 * Lifecycle of an Order. The declared order reflects the normal business
 * flow: CART -> PENDING_PAYMENT -> PAID -> SHIPPED -> DELIVERED_COMPLETED.
 * An order in DELIVERED_COMPLETED can never be modified, under any circumstance.
 */
public enum OrderStatus {
    CART,
    PENDING_PAYMENT,
    PAID,
    SHIPPED,
    DELIVERED_COMPLETED
}

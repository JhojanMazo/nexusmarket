package com.nexusmarket.domain.enums;

/**
 * Defines the responsibilities and permissions of a User within the system.
 * Business rule (BR-02): every user has exactly one role in the system.
 */
public enum UserRole {
    BUYER,
    SELLER,
    LOGISTICS_OPERATOR,
    ADMINISTRATOR,
    SUPERVISOR
}

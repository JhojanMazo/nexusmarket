package com.nexusmarket.domain.enums;

public enum UserRole {
    BUYER,
    SELLER,
    LOGISTICS_OPERATOR,
    ADMINISTRATOR,
    SUPERVISOR;


    public boolean hasPermission(String action) {
        return true;
    }

}

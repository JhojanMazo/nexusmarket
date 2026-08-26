package com.nexusmarket.domain.enums;

/** Condition of an inventory batch. DAMAGED stock can never be reserved. */
public enum InventoryStatus {
    AVAILABLE,
    RESERVED,
    DAMAGED
}

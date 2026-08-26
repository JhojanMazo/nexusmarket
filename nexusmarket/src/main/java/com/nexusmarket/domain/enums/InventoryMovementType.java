package com.nexusmarket.domain.enums;

/** Types of movement an Inventory record can undergo. */
public enum InventoryMovementType {
    INBOUND,
    RESERVATION,
    SALE_OUTBOUND,
    ADJUSTMENT,
    RETURN
}

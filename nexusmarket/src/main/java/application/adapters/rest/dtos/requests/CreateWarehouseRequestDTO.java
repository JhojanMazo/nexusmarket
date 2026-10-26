package application.adapters.rest.dtos.requests;

import application.domain.enums.WarehouseType;

import java.util.UUID;

public record CreateWarehouseRequestDTO(
        String name,
        AddressDTO address,
        WarehouseType type,
        UUID sellerId,
        double capacity
) {
}

package application.adapters.rest.dtos.responses;

import application.adapters.rest.dtos.requests.AddressDTO;
import application.domain.enums.WarehouseType;

import java.util.UUID;

public record WarehouseResponseDTO(
        UUID warehouseId,
        String name,
        AddressDTO address,
        WarehouseType type,
        UUID sellerId,
        double capacity
) {
}

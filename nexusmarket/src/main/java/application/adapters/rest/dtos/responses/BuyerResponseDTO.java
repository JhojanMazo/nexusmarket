package application.adapters.rest.dtos.responses;
import application.adapters.rest.dtos.requests.AddressDTO;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;

import java.util.List;
import java.util.UUID;

public record BuyerResponseDTO(
        UUID userId,
        String fullName,
        String email,
        String documentType,
        String documentNumber,
        UserRole role,
        UserStatus status,
        AddressDTO primaryAddress,
        List<AddressDTO> additionalAddresses,
        BuyerCommercialStatus commercialStatus
) {
}

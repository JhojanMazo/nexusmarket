package application.adapters.rest.dtos.responses;
import application.adapters.rest.dtos.requests.AddressDTO;

import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;

import java.util.UUID;

public record UserResponseDTO(
        UUID userId,
        String fullName,
        String email,
        String documentType,
        String documentNumber,
        UserRole role,
        UserStatus status
) {
}

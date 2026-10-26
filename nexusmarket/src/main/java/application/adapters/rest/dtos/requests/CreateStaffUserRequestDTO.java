package application.adapters.rest.dtos.requests;
import application.adapters.rest.dtos.requests.AddressDTO;

import application.domain.enums.UserRole;

public record CreateStaffUserRequestDTO(
        String fullName,
        String email,
        String documentType,
        String documentNumber,
        UserRole role
) {
}

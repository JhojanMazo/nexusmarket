package application.adapters.rest.dtos.requests;
import application.adapters.rest.dtos.requests.AddressDTO;

import application.domain.enums.UserStatus;

public record UpdateUserStatusRequestDTO(
        UserStatus status
) {
}

package application.adapters.rest.dtos.responses;
import application.adapters.rest.dtos.requests.AddressDTO;

import application.domain.enums.UserRole;

import java.util.UUID;

public record LoginResponseDTO(
        String token,
        String tokenType,
        UUID userId,
        String email,
        UserRole role
) {
    public static LoginResponseDTO bearer(String token, UUID userId, String email, UserRole role) {
        return new LoginResponseDTO(token, "Bearer", userId, email, role);
    }
}

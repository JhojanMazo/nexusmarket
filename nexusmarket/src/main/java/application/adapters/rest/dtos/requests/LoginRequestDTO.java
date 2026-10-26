package application.adapters.rest.dtos.requests;
import application.adapters.rest.dtos.requests.AddressDTO;

public record LoginRequestDTO(
        String email,
        String password
) {
}

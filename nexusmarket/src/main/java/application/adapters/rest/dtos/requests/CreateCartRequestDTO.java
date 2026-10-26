package application.adapters.rest.dtos.requests;
import application.adapters.rest.dtos.requests.AddressDTO;

import java.util.UUID;

public record CreateCartRequestDTO(
        UUID buyerId
) {
}

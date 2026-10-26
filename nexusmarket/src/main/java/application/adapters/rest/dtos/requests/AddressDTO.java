package application.adapters.rest.dtos.requests;
import application.adapters.rest.dtos.requests.AddressDTO;

public record AddressDTO(
        String street,
        String city,
        String stateOrProvince,
        String country,
        String postalCode,
        String reference
) {
}

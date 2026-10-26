package application.adapters.rest.dtos.requests;

import java.util.List;

public record UpdateBuyerAddressesRequestDTO(
        AddressDTO primaryAddress,
        List<AddressDTO> additionalAddresses
) {
}

package application.adapters.rest.dtos.requests;
import application.adapters.rest.dtos.requests.AddressDTO;

public record CreateBuyerRequestDTO(
        String fullName,
        String email,
        String documentType,
        String documentNumber,
        AddressDTO primaryAddress
) {
}

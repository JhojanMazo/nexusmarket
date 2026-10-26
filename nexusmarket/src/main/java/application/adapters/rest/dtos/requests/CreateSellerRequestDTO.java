package application.adapters.rest.dtos.requests;

import java.time.LocalDate;
import java.util.UUID;

public record CreateSellerRequestDTO(
        UUID adminUserId,
        String fullName,
        String email,
        String documentType,
        String documentNumber,
        LocalDate onboardingDate,
        String warehouseName,
        AddressDTO warehouseAddress,
        Double warehouseCapacity
) {
    public CreateSellerRequestDTO(UUID adminUserId, String fullName, String email,
                                 String documentType, String documentNumber, LocalDate onboardingDate) {
        this(adminUserId, fullName, email, documentType, documentNumber, onboardingDate, null, null, null);
    }
}

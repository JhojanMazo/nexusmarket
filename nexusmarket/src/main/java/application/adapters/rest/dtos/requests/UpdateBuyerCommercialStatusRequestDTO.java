package application.adapters.rest.dtos.requests;

import application.domain.enums.BuyerCommercialStatus;

public record UpdateBuyerCommercialStatusRequestDTO(
        BuyerCommercialStatus commercialStatus
) {
}

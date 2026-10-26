package application.adapters.rest.dtos.requests;
import application.adapters.rest.dtos.requests.AddressDTO;

import application.domain.enums.OrderStatus;

public record UpdateOrderStatusRequestDTO(
        OrderStatus status
) {
}

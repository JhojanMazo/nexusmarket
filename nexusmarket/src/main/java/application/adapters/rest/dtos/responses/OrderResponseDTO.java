package application.adapters.rest.dtos.responses;
import application.adapters.rest.dtos.requests.AddressDTO;

import application.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(
        UUID orderId,
        UUID buyerId,
        String buyerName,
        List<OrderItemResponseDTO> items,
        AddressDTO deliveryAddress,
        OrderStatus status,
        LocalDate creationDate,
        BigDecimal subtotal,
        BigDecimal total,
        String currency
) {
}

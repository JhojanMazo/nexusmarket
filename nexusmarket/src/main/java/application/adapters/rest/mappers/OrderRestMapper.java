package application.adapters.rest.mappers;
import application.adapters.rest.dtos.requests.AddressDTO;

import application.domain.models.Order;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import application.adapters.rest.dtos.requests.AddressDTO;
import application.adapters.rest.dtos.responses.OrderItemResponseDTO;
import application.adapters.rest.dtos.responses.OrderResponseDTO;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class OrderRestMapper {

    public OrderRestMapper() {
    }

    public static List<OrderResponseDTO> toResponseList(List<Order> orders) {
        if (orders == null) {
            return Collections.emptyList();
        }
        return orders.stream().map(OrderRestMapper::toResponse).toList();
    }

    public static OrderResponseDTO toResponse(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponseDTO> itemResponses = order.getItems().stream()
                .map(OrderRestMapper::toItemResponse)
                .toList();

        Money subtotal = order.subtotal();
        Money total = order.total();

        return new OrderResponseDTO(
                order.getOrderId(),
                order.getBuyer() != null ? order.getBuyer().getUserId() : null,
                order.getBuyer() != null ? order.getBuyer().getFullName() : null,
                itemResponses,
                toAddressDto(order.getDeliveryAddress()),
                order.getStatus(),
                order.getCreationDate(),
                subtotal.amount(),
                total.amount(),
                total.currency().getCurrencyCode()
        );
    }

    public static OrderItemResponseDTO toItemResponse(OrderItem item) {
        if (item == null) {
            return null;
        }
        Money subtotal = item.subtotal();
        return new OrderItemResponseDTO(
                item.product().getProductId(),
                item.product().getName(),
                item.quantity(),
                item.unitPrice().amount(),
                subtotal.amount(),
                subtotal.currency().getCurrencyCode()
        );
    }

    public static AddressDTO toAddressDto(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressDTO(
                address.street(),
                address.city(),
                address.stateOrProvince(),
                address.country(),
                address.postalCode(),
                address.reference()
        );
    }

    public static Address toAddress(AddressDTO dto) {
        if (dto == null) {
            return null;
        }
        return new Address(
                dto.street(),
                dto.city(),
                dto.stateOrProvince(),
                dto.country(),
                dto.postalCode(),
                dto.reference()
        );
    }
}

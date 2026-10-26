package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.CartItemResponseDTO;
import application.adapters.rest.dtos.responses.ShoppingCartResponseDTO;
import application.domain.models.ShoppingCart;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartRestMapper {

    public CartRestMapper() {
    }

    public static ShoppingCartResponseDTO toResponse(ShoppingCart cart) {
        if (cart == null) {
            return null;
        }

        List<CartItemResponseDTO> itemResponses = cart.getItems().stream()
                .map(CartRestMapper::toItemResponse)
                .toList();

        Money subtotal = cart.subtotal();

        return new ShoppingCartResponseDTO(
                cart.getCartId(),
                cart.getBuyer() != null ? cart.getBuyer().getUserId() : null,
                cart.getBuyer() != null ? cart.getBuyer().getFullName() : null,
                itemResponses,
                subtotal.amount(),
                subtotal.currency().getCurrencyCode()
        );
    }

    public static CartItemResponseDTO toItemResponse(OrderItem item) {
        if (item == null) {
            return null;
        }
        Money subtotal = item.subtotal();
        return new CartItemResponseDTO(
                item.product().getProductId(),
                item.product().getName(),
                item.quantity(),
                item.unitPrice().amount(),
                subtotal.amount(),
                subtotal.currency().getCurrencyCode()
        );
    }
}

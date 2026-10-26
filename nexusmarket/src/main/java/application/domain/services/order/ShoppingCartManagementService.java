package application.domain.services.order;

import application.domain.enums.ProductStatus;
import application.domain.models.Product;
import application.domain.models.ShoppingCart;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 5.3 Manages pre-checkout buyer carts.
 */
@Service
public class ShoppingCartManagementService {

    public void addItem(ShoppingCart cart, Product product, int quantity, Money unitPrice) {
        Objects.requireNonNull(cart, "ShoppingCart must not be null");
        Objects.requireNonNull(product, "Product must not be null");
        if (product.getStatus() != ProductStatus.PUBLISHED) {
            throw new IllegalStateException("Solo se pueden agregar productos publicados al carrito");
        }
        Money price = unitPrice != null ? unitPrice : product.getPrice();
        cart.addItem(new OrderItem(product, quantity, price));
    }
}

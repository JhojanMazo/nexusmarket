package application.domain.services.order;

import application.domain.enums.BuyerCommercialStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ShoppingCart;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderItem;
import application.domain.valueobjects.ProductVariant;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.UUID;

public final class OrderTestSupport {

    private OrderTestSupport() {
    }

    public static Order createTestOrder() {
        Address address = new Address("Calle 10", "Medellin", "Antioquia", "Colombia", "05001", null);
        Buyer buyer = Buyer.create(
                "Order Test Buyer",
                new Email("orderbuyer@test.com"),
                new IdentityDocument("CC", "554433"),
                address,
                Collections.emptyList(),
                BuyerCommercialStatus.ENABLED
        );

        ShoppingCart cart = new ShoppingCart(UUID.randomUUID(), buyer);
        Product product = new Product(
                UUID.randomUUID(),
                "Support Laptop",
                new ProductVariant("Spec", "Standard"),
                new Money(BigDecimal.valueOf(500), Currency.getInstance("USD")),
                ProductStatus.PUBLISHED,
                ProductType.PHYSICAL
        );
        cart.addItem(new OrderItem(product, 2, new Money(BigDecimal.valueOf(500), Currency.getInstance("USD"))));

        return Order.createFromCart(cart, address);
    }
}

package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.BuyerCommercialStatus;
import com.nexusmarket.domain.valueobject.Address;
import com.nexusmarket.domain.valueobject.Email;
import com.nexusmarket.domain.valueobject.IdentityDocument;
import com.nexusmarket.domain.valueobject.Money;
import com.nexusmarket.domain.valueobject.OrderItem;
import com.nexusmarket.domain.valueobject.ProductVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {
    private Buyer buyer;
    private ShoppingCart cart;
    private OrderItem item1;
    private OrderItem item2;

    @BeforeEach
    void setUp() {
        buyer = Buyer.create(
                "John Doe",
                new Email("john@example.com"),
                new IdentityDocument("DNI", "12345678"),
                new Address("Main St 1", "City", "State", "Country", "12345", null),
                List.of(),
                BuyerCommercialStatus.ENABLED);
        cart = new ShoppingCart(java.util.UUID.randomUUID(), buyer);
        // Simple product placeholder (use null for product reference, as OrderItem only needs a Product instance)
        // We'll create a minimal Product instance for the test.
        Product product = new Product(java.util.UUID.randomUUID(), "Test Product",
                new ProductVariant("Color", "Red"),
                new Money(BigDecimal.valueOf(10), Currency.getInstance("USD")),
                com.nexusmarket.domain.enums.ProductStatus.PUBLISHED,
                com.nexusmarket.domain.enums.ProductType.PHYSICAL);
        item1 = new OrderItem(product, 2, new Money(BigDecimal.valueOf(10), Currency.getInstance("USD")));
        item2 = new OrderItem(product, 1, new Money(BigDecimal.valueOf(15), Currency.getInstance("USD")));
    }

    @Test
    void addAndRemoveItems() {
        assertTrue(cart.getItems().isEmpty());
        cart.addItem(item1);
        cart.addItem(item2);
        assertEquals(2, cart.getItems().size());
        cart.removeItem(item1);
        assertEquals(1, cart.getItems().size());
        assertEquals(item2, cart.getItems().get(0));
    }

    @Test
    void subtotalCalculation() {
        // Empty cart subtotal should be zero USD
        Money empty = cart.subtotal();
        assertEquals(BigDecimal.ZERO, empty.amount());
        assertEquals(Currency.getInstance("USD"), empty.currency());

        cart.addItem(item1); // 2 * 10 = 20
        cart.addItem(item2); // 1 * 15 = 15
        Money total = cart.subtotal();
        assertEquals(BigDecimal.valueOf(35), total.amount());
        assertEquals(Currency.getInstance("USD"), total.currency());
    }
}

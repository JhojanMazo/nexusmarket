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

class OrderTest {
    private Buyer buyer;
    private ShoppingCart cart;
    private Product product;
    private OrderItem item1;
    private OrderItem item2;
    private Address deliveryAddress;

    @BeforeEach
    void setUp() {
        buyer = Buyer.create(
                "Alice Buyer",
                new Email("alice@example.com"),
                new IdentityDocument("DNI", "87654321"),
                new Address("Avenue 10", "City", "State", "Country", "54321", null),
                List.of(),
                BuyerCommercialStatus.ENABLED);
        cart = new ShoppingCart(java.util.UUID.randomUUID(), buyer);
        product = new Product(java.util.UUID.randomUUID(), "Gadget", new ProductVariant("Color", "Blue"),
                new Money(BigDecimal.valueOf(20), Currency.getInstance("USD")),
                com.nexusmarket.domain.enums.ProductStatus.PUBLISHED,
                com.nexusmarket.domain.enums.ProductType.PHYSICAL);
        item1 = new OrderItem(product, 2, new Money(BigDecimal.valueOf(20), Currency.getInstance("USD")));
        item2 = new OrderItem(product, 1, new Money(BigDecimal.valueOf(30), Currency.getInstance("USD")));
        deliveryAddress = new Address("Street 5", "City", "State", "Country", "11111", null);
    }

    @Test
    void createOrderFromCartCopiesItems() {
        cart.addItem(item1);
        cart.addItem(item2);
        Order order = Order.createFromCart(cart, deliveryAddress);
        assertEquals(2, order.getItems().size());
        assertTrue(order.getItems().contains(item1));
        assertTrue(order.getItems().contains(item2));
        // modify cart afterwards – order should stay unchanged
        cart.addItem(new OrderItem(product, 3, new Money(BigDecimal.valueOf(20), Currency.getInstance("USD"))));
        assertEquals(2, order.getItems().size(), "Order items must be immutable after creation");
    }

    @Test
    void subtotalAndTotalCalculation() {
        cart.addItem(item1); // 2 * 20 = 40
        cart.addItem(item2); // 1 * 30 = 30
        Order order = Order.createFromCart(cart, deliveryAddress);
        assertEquals(BigDecimal.valueOf(70), order.subtotal().amount());
        assertEquals(order.subtotal(), order.total(), "Total currently equals subtotal");
    }

    @Test
    void validStateTransitions() {
        cart.addItem(item1);
        Order order = Order.createFromCart(cart, deliveryAddress);
        assertEquals(com.nexusmarket.domain.enums.OrderStatus.CART, order.getStatus());
        order.advanceTo(com.nexusmarket.domain.enums.OrderStatus.PENDING_PAYMENT);
        assertEquals(com.nexusmarket.domain.enums.OrderStatus.PENDING_PAYMENT, order.getStatus());
        order.advanceTo(com.nexusmarket.domain.enums.OrderStatus.PAID);
        order.advanceTo(com.nexusmarket.domain.enums.OrderStatus.SHIPPED);
        order.advanceTo(com.nexusmarket.domain.enums.OrderStatus.DELIVERED_COMPLETED);
        assertEquals(com.nexusmarket.domain.enums.OrderStatus.DELIVERED_COMPLETED, order.getStatus());
    }

    @Test
    void invalidStateTransitionThrows() {
        cart.addItem(item1);
        Order order = Order.createFromCart(cart, deliveryAddress);
        // direct jump from CART to PAID is illegal
        assertThrows(IllegalStateException.class, () -> order.advanceTo(com.nexusmarket.domain.enums.OrderStatus.PAID));
        // valid first transition then illegal skip
        order.advanceTo(com.nexusmarket.domain.enums.OrderStatus.PENDING_PAYMENT);
        assertThrows(IllegalStateException.class, () -> order.advanceTo(com.nexusmarket.domain.enums.OrderStatus.SHIPPED));
    }

    @Test
    void cannotCreateOrderFromEmptyCart() {
        assertThrows(IllegalArgumentException.class, () -> Order.createFromCart(cart, deliveryAddress));
    }
}

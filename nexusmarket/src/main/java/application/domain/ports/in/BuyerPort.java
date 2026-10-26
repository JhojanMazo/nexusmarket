package application.domain.ports.in;

import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.ReturnRequest;
import application.domain.models.ShoppingCart;
import application.domain.valueobjects.Address;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BuyerPort {
    Buyer registerBuyer(String fullName, String email, String docType, String docNumber, Address primaryAddress);
    Buyer getBuyerProfile(UUID buyerId);

    Buyer addAdditionalAddress(UUID buyerId, Address address);
    Buyer addAdditionalAddress(UUID authenticatedBuyerId, UUID buyerId, Address address);
    Buyer updateAdditionalAddress(UUID authenticatedBuyerId, UUID buyerId, int index, Address newAddress);
    Buyer removeAdditionalAddress(UUID authenticatedBuyerId, UUID buyerId, int index);
    Buyer updateAddresses(UUID buyerId, Address primaryAddress, List<Address> additionalAddresses);
    Buyer updateAddresses(UUID authenticatedBuyerId, UUID buyerId, Address primaryAddress, List<Address> additionalAddresses);

    ShoppingCart createCart(UUID buyerId);
    ShoppingCart getCartById(UUID cartId);
    ShoppingCart getCartByBuyerId(UUID buyerId);
    ShoppingCart addItem(UUID cartId, UUID productId, int quantity, BigDecimal unitPrice, String currencyCode);
    ShoppingCart removeItem(UUID cartId, UUID productId);
    void clearCart(UUID cartId);

    Order createOrderFromCart(UUID cartId, Address deliveryAddress);
    Order getOrderById(UUID orderId);
    List<Order> getOrdersByBuyerId(UUID buyerId);
    Order updateDeliveryAddress(UUID orderId, Address newAddress);

    Order processPayment(UUID orderId, BigDecimal amount, String paymentMethod);
    ReturnRequest requestReturn(UUID orderId, String reason);
    List<ReturnRequest> getBuyerReturns(UUID buyerId);
}

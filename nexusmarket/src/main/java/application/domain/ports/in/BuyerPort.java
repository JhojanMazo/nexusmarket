package application.domain.ports.in;

import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ReturnRequest;
import application.domain.models.ShoppingCart;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.PageResult;

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
    default ShoppingCart updateItemQuantity(UUID cartId, UUID productId, int quantity) {
        if (quantity <= 0) {
            return removeItem(cartId, productId);
        }
        removeItem(cartId, productId);
        return addItem(cartId, productId, quantity, null, null);
    }
    ShoppingCart removeItem(UUID cartId, UUID productId);
    void clearCart(UUID cartId);

    Order createOrderFromCart(UUID cartId, Address deliveryAddress);
    Order getOrderById(UUID orderId);
    List<Order> getOrdersByBuyerId(UUID buyerId);
    default PageResult<Order> getOrdersByBuyerId(UUID buyerId, int page, int size, String sortBy, String direction) {
        return PageResult.of(getOrdersByBuyerId(buyerId), page, size);
    }
    default PageResult<Order> getOrdersByBuyerId(UUID buyerId, int page, int size) {
        return getOrdersByBuyerId(buyerId, page, size, "orderDate", "DESC");
    }
    default PageResult<Product> getProductsCatalog(int page, int size) {
        return PageResult.of(List.of(), page, size);
    }
    default Order cancelOrder(UUID orderId) {
        Order order = getOrderById(orderId);
        order.cancel();
        return order;
    }
    Order updateDeliveryAddress(UUID orderId, Address newAddress);

    Order processPayment(UUID orderId, BigDecimal amount, String paymentMethod);
    ReturnRequest requestReturn(UUID orderId, String reason);
    List<ReturnRequest> getBuyerReturns(UUID buyerId);
}

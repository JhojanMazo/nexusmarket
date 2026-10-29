package application.domain.ports.out;

import application.domain.models.Order;
import application.domain.valueobjects.PageResult;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for Order aggregates.
 */
public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(UUID orderId);
    List<Order> findByBuyerId(UUID buyerId);
    List<Order> findAll();

    default PageResult<Order> findAll(int page, int size, String sortBy, String direction) {
        List<Order> sorted = sortOrders(findAll(), sortBy, direction);
        return PageResult.of(sorted, page, size);
    }

    default PageResult<Order> findAll(int page, int size) {
        return findAll(page, size, "orderDate", "DESC");
    }

    default PageResult<Order> findByBuyerId(UUID buyerId, int page, int size, String sortBy, String direction) {
        List<Order> sorted = sortOrders(findByBuyerId(buyerId), sortBy, direction);
        return PageResult.of(sorted, page, size);
    }

    default PageResult<Order> findByBuyerId(UUID buyerId, int page, int size) {
        return findByBuyerId(buyerId, page, size, "orderDate", "DESC");
    }

    private static List<Order> sortOrders(List<Order> orders, String sortBy, String direction) {
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }
        Comparator<Order> comparator;
        if ("status".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(o -> o.getStatus().name());
        } else if ("orderId".equalsIgnoreCase(sortBy) || "id".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Order::getOrderId);
        } else {
            comparator = Comparator.comparing(Order::getCreationDate, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if ("DESC".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }
        return orders.stream().sorted(comparator).toList();
    }
}

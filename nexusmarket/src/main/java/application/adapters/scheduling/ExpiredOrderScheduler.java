package application.adapters.scheduling;

import application.domain.models.Order;
import application.domain.ports.out.EventPublisherPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.inventory.InventoryReservationService;
import application.domain.services.order.ExpiredOrderCleanupScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled adapter running every 60 seconds ({@code fixedRate = 60000}) to cancel
 * expired {@code PENDING_PAYMENT} orders and release their reserved stock.
 */
@Component
public class ExpiredOrderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiredOrderScheduler.class);

    private final ExpiredOrderCleanupScheduler cleanupScheduler;

    @Autowired
    public ExpiredOrderScheduler(ExpiredOrderCleanupScheduler cleanupScheduler) {
        this.cleanupScheduler = cleanupScheduler;
    }

    public ExpiredOrderScheduler(
            OrderRepositoryPort orderRepositoryPort,
            InventoryRepositoryPort inventoryRepositoryPort,
            InventoryReservationService inventoryReservationService,
            EventPublisherPort eventPublisherPort,
            @Value("${nexusmarket.orders.ttl-minutes:30}") long ttlMinutes) {
        this.cleanupScheduler = new ExpiredOrderCleanupScheduler(
                orderRepositoryPort,
                inventoryRepositoryPort,
                inventoryReservationService,
                eventPublisherPort,
                ttlMinutes
        );
    }

    public ExpiredOrderScheduler(
            OrderRepositoryPort orderRepositoryPort,
            InventoryRepositoryPort inventoryRepositoryPort,
            InventoryReservationService inventoryReservationService,
            EventPublisherPort eventPublisherPort) {
        this(orderRepositoryPort, inventoryRepositoryPort, inventoryReservationService, eventPublisherPort, 30L);
    }

    @Scheduled(fixedRate = 60000)
    public List<Order> releaseExpiredOrders() {
        List<Order> cancelled = cleanupScheduler.cleanupExpiredOrders();
        if (!cancelled.isEmpty()) {
            log.info("[TTL SCHEDULER] Cancelled {} expired PENDING_PAYMENT order(s) and released stock.",
                    cancelled.size());
        }
        return cancelled;
    }
}

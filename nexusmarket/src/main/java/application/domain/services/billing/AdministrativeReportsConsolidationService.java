package application.domain.services.billing;

import application.domain.models.AdministrativeReport;
import application.domain.models.Inventory;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.models.Warehouse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 6.5 Aggregates platform operational telemetry for reporting (OBJ-12).
 */
@Service
public class AdministrativeReportsConsolidationService {

    public String generatePlatformReport(List<Order> orders, List<Product> products, List<User> users) {
        int orderCount = orders != null ? orders.size() : 0;
        int productCount = products != null ? products.size() : 0;
        int userCount = users != null ? users.size() : 0;

        return String.format(
                "NexusMarket Platform Telemetry Report: [Orders: %d, Products: %d, Users: %d]",
                orderCount, productCount, userCount
        );
    }

    public AdministrativeReport generateConsolidatedReport(
            List<User> users,
            List<Product> products,
            List<Warehouse> warehouses,
            List<Order> orders,
            List<Shipment> shipments,
            List<ReturnRequest> returns,
            List<Refund> refunds) {
        return generateConsolidatedReport(users, products, warehouses, Collections.emptyList(),
                orders, Collections.emptyList(), shipments, returns, refunds);
    }

    public AdministrativeReport generateConsolidatedReport(
            List<User> users,
            List<Product> products,
            List<Warehouse> warehouses,
            List<Inventory> inventories,
            List<Order> orders,
            List<Invoice> invoices,
            List<Shipment> shipments,
            List<ReturnRequest> returns,
            List<Refund> refunds) {

        long totalUsers = users != null ? users.size() : 0;
        Map<String, Long> usersByRole = users != null
                ? users.stream().collect(Collectors.groupingBy(u -> u.getRole().name(), Collectors.counting()))
                : Collections.emptyMap();

        long totalProducts = products != null ? products.size() : 0;
        long totalWarehouses = warehouses != null ? warehouses.size() : 0;
        long totalOrders = orders != null ? orders.size() : 0;

        Map<String, Long> ordersByStatus = orders != null
                ? orders.stream().collect(Collectors.groupingBy(o -> o.getStatus().name(), Collectors.counting()))
                : Collections.emptyMap();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (orders != null) {
            for (Order o : orders) {
                if (o.getItems() != null && !o.getItems().isEmpty()) {
                    totalRevenue = totalRevenue.add(o.total().amount());
                }
            }
        }

        long totalInvoices = invoices != null ? invoices.size() : 0;
        BigDecimal totalInvoicedAmount = BigDecimal.ZERO;
        if (invoices != null) {
            for (Invoice inv : invoices) {
                if (inv.getTotal() != null) {
                    totalInvoicedAmount = totalInvoicedAmount.add(inv.getTotal().amount());
                }
            }
        }

        long totalInventoryAvailableUnits = inventories != null
                ? inventories.stream().mapToLong(Inventory::getAvailableQuantity).sum()
                : 0L;
        long totalInventoryReservedUnits = inventories != null
                ? inventories.stream().mapToLong(Inventory::getReservedQuantity).sum()
                : 0L;
        Map<String, Long> inventoryLotsByStatus = inventories != null
                ? inventories.stream().collect(Collectors.groupingBy(i -> i.getStatus().name(), Collectors.counting()))
                : Collections.emptyMap();

        long totalShipments = shipments != null ? shipments.size() : 0;

        long totalReturns = returns != null ? returns.size() : 0;
        Map<String, Long> returnsByStatus = returns != null
                ? returns.stream().collect(Collectors.groupingBy(r -> r.getStatus().name(), Collectors.counting()))
                : Collections.emptyMap();

        long totalRefunds = refunds != null ? refunds.size() : 0;
        Map<String, Long> refundsByStatus = refunds != null
                ? refunds.stream().collect(Collectors.groupingBy(r -> r.getStatus().name(), Collectors.counting()))
                : Collections.emptyMap();
        BigDecimal totalRefundedAmount = BigDecimal.ZERO;
        if (refunds != null) {
            for (Refund ref : refunds) {
                if (ref.getStatus() == Refund.RefundStatus.PROCESSED && ref.getAmount() != null) {
                    totalRefundedAmount = totalRefundedAmount.add(ref.getAmount().amount());
                }
            }
        }

        return new AdministrativeReport(
                totalUsers,
                usersByRole,
                totalProducts,
                totalWarehouses,
                totalOrders,
                ordersByStatus,
                totalRevenue,
                totalInvoices,
                totalInvoicedAmount,
                totalInventoryAvailableUnits,
                totalInventoryReservedUnits,
                inventoryLotsByStatus,
                totalShipments,
                totalReturns,
                returnsByStatus,
                totalRefunds,
                refundsByStatus,
                totalRefundedAmount
        );
    }
}

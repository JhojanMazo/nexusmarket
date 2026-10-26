package application.domain.models;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

public record AdministrativeReport(
        long totalUsers,
        Map<String, Long> usersByRole,
        long totalProducts,
        long totalWarehouses,
        long totalOrders,
        Map<String, Long> ordersByStatus,
        BigDecimal totalRevenue,
        long totalInvoices,
        BigDecimal totalInvoicedAmount,
        long totalInventoryAvailableUnits,
        long totalInventoryReservedUnits,
        Map<String, Long> inventoryLotsByStatus,
        long totalShipments,
        long totalReturns,
        Map<String, Long> returnsByStatus,
        long totalRefunds,
        Map<String, Long> refundsByStatus,
        BigDecimal totalRefundedAmount
) {
    public AdministrativeReport(
            long totalUsers,
            Map<String, Long> usersByRole,
            long totalProducts,
            long totalWarehouses,
            long totalOrders,
            Map<String, Long> ordersByStatus,
            BigDecimal totalRevenue,
            long totalShipments,
            long totalReturns,
            long totalRefunds
    ) {
        this(
                totalUsers,
                usersByRole,
                totalProducts,
                totalWarehouses,
                totalOrders,
                ordersByStatus,
                totalRevenue,
                0L,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                0L,
                0L,
                Collections.emptyMap(),
                totalShipments,
                totalReturns,
                Collections.emptyMap(),
                totalRefunds,
                Collections.emptyMap(),
                BigDecimal.ZERO
        );
    }
}

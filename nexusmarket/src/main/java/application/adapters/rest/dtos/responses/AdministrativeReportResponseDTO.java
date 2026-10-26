package application.adapters.rest.dtos.responses;

import application.domain.models.AdministrativeReport;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

public record AdministrativeReportResponseDTO(
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
    public AdministrativeReportResponseDTO(
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

    public static AdministrativeReportResponseDTO fromDomain(AdministrativeReport report) {
        if (report == null) return null;
        return new AdministrativeReportResponseDTO(
                report.totalUsers(),
                report.usersByRole(),
                report.totalProducts(),
                report.totalWarehouses(),
                report.totalOrders(),
                report.ordersByStatus(),
                report.totalRevenue(),
                report.totalInvoices(),
                report.totalInvoicedAmount(),
                report.totalInventoryAvailableUnits(),
                report.totalInventoryReservedUnits(),
                report.inventoryLotsByStatus(),
                report.totalShipments(),
                report.totalReturns(),
                report.returnsByStatus(),
                report.totalRefunds(),
                report.refundsByStatus(),
                report.totalRefundedAmount()
        );
    }
}

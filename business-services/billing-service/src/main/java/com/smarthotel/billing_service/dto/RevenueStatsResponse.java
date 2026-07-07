package com.smarthotel.billing_service.dto;

import java.math.BigDecimal;

/**
 * Thống kê doanh thu tổng hợp cho màn hình Dashboard của Admin.
 * Các chỉ số doanh thu chỉ tính trên các hóa đơn đã thanh toán (PAID).
 */
public record RevenueStatsResponse(
        long totalInvoices,
        long paidInvoices,
        long unpaidInvoices,
        BigDecimal totalRoomRevenue,
        BigDecimal totalServiceRevenue,
        BigDecimal totalTax,
        BigDecimal totalRevenue) {}

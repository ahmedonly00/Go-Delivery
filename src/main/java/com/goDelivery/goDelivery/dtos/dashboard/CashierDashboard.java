package com.goDelivery.goDelivery.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashierDashboard {

    // Metadata
    private Long cashierId;
    private String cashierName;
    private Long restaurantId;
    private String restaurantName;
    private LocalDateTime generatedAt;
    private LocalDate date;

    // Today's Summary
    private TodaySummary todaySummary;

    // Order Management
    private OrderManagement orderManagement;

    // Payment Breakdown
    private PaymentBreakdown paymentBreakdown;

    // Shift Summary
    private ShiftSummary shiftSummary;

    // Recent Orders
    private List<RecentOrder> recentOrders;

    // ============ Nested Classes ============

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodaySummary {
        private Long ordersProcessedToday;
        private BigDecimal totalCashCollected;
        private BigDecimal totalCardPayments;
        private BigDecimal totalMobilePayments;
        private Long ordersPendingPayment;
        private BigDecimal totalRevenueToday;
        private BigDecimal averageOrderValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderManagement {
        private Long activeOrders;
        private Long pendingOrders;
        private Long completedOrdersToday;
        private Long cancelledOrdersToday;
        private Map<String, Long> ordersByStatus;
        private Double averageProcessingTime; // in minutes
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentBreakdown {
        private CashPayments cashPayments;
        private MobilePayments mobilePayments;
        private PendingPayments pendingPayments;
        private BigDecimal totalPaymentsCollected;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashPayments {
        private Long count;
        private BigDecimal total;
        private BigDecimal averageAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MobilePayments {
        private Long momoCount;
        private BigDecimal momoTotal;
        private Long mpesaCount;
        private BigDecimal mpesaTotal;
        private BigDecimal totalMobilePayments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingPayments {
        private Long count;
        private BigDecimal total;
        private List<PendingPaymentDetail> details;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingPaymentDetail {
        private Long orderId;
        private String customerName;
        private BigDecimal amount;
        private String paymentMethod;
        private LocalDateTime orderTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShiftSummary {
        private LocalDateTime shiftStartTime;
        private LocalDateTime shiftEndTime;
        private Long ordersHandled;
        private BigDecimal revenueCollected;
        private Double averageOrderProcessingTime;
        private Long totalTransactions;
        private Map<String, BigDecimal> paymentMethodBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrder {
        private Long orderId;
        private String customerName;
        private String orderStatus;
        private BigDecimal totalAmount;
        private String paymentMethod;
        private String paymentStatus;
        private LocalDateTime orderTime;
        private Integer itemCount;
    }
}

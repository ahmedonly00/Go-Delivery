package com.goDelivery.goDelivery.dtos.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDashboard {
    private String restaurantId;
    private String restaurantName;
    private String period; // DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Order Metrics
    private int totalOrders;
    private int completedOrders;
    private int cancelledOrders;
    private BigDecimal orderCompletionRate;
    private BigDecimal averagePreparationTime; // in minutes
    private Map<String, Integer> ordersByStatus;
    private Map<String, Integer> ordersByType;
    
    // Financial Metrics
    private BigDecimal totalRevenue;
    private BigDecimal netRevenue;
    private BigDecimal averageOrderValue;
    private BigDecimal totalTaxes;
    private BigDecimal totalFees;
    private Map<String, BigDecimal> revenueByCategory;
    private Map<String, BigDecimal> revenueByPaymentMethod;
    
    // Menu Performance
    private List<MenuItemPerformance> topPerformingItems;
    private List<MenuItemPerformance> lowPerformingItems;
    private List<CategoryPerformance> categoryPerformance;
    
    // Customer Metrics
    private int totalCustomers;
    private int newCustomers;
    private int returningCustomers;
    private BigDecimal averageCustomerRating;
    private int totalReviews;
    private Map<Integer, Integer> ratingDistribution; // Key: rating (1-5), Value: count
    
    // Delivery Metrics
    private double averageDeliveryTime; // in minutes
    private Map<String, Integer> deliveryZones;
    private List<DeliveryPerformance> deliveryPerformance;
    
    // Time-based Metrics
    private List<TimeSlotPerformance> timeSlotPerformance;
    private Map<Integer, Integer> ordersByHour; // 0-23
    private Map<String, Integer> ordersByDayOfWeek;
    
    // Staff Performance
    private List<StaffPerformance> staffPerformance;
    
    // Promotions
    private List<PromotionPerformance> promotionPerformance;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemPerformance {
        private String itemId;
        private String itemName;
        private String category;
        private int quantitySold;
        private BigDecimal totalRevenue;
        private BigDecimal profitMargin;
        private int timesReturned;
        private double averageRating;
        private int totalReviews;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryPerformance {
        private String categoryId;
        private String categoryName;
        private int itemsSold;
        private BigDecimal totalRevenue;
        private BigDecimal profitMargin;
        private int totalOrders;
        private double averageRating;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryPerformance {
        private String zoneId;
        private String zoneName;
        private int orderCount;
        private double averageDeliveryTime;
        private double onTimeDeliveryRate;
        private BigDecimal averageOrderValue;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotPerformance {
        private String timeSlot; // e.g., "Breakfast", "Lunch", "Dinner"
        private int orderCount;
        private BigDecimal totalRevenue;
        private double averageOrderValue;
        private double averagePreparationTime;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffPerformance {
        private String staffId;
        private String staffName;
        private String role;
        private int ordersHandled;
        private double averagePreparationTime;
        private double averageRating;
        private int totalShifts;
        private double averageHoursWorked;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromotionPerformance {
        private String promotionId;
        private String promotionName;
        private int timesUsed;
        private BigDecimal totalDiscountAmount;
        private BigDecimal incrementalRevenue;
        private int newCustomersAcquired;
    }
}

package com.goDelivery.goDelivery.dtos.admin;

import com.goDelivery.goDelivery.Enum.StatsPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatsDTO {

    // Filter applied
    private StatsPeriod period;

    // Restaurants
    private long totalRestaurants;
    private long approvedRestaurants;
    private long pendingRestaurants;

    // Staff (restaurant-level users + branch-level users)
    private long totalStaffMembers;
    private long restaurantStaff;
    private long branchStaff;

    // Orders (paymentStatus = PAID OR orderStatus = CONFIRMED)
    private long totalOrders;

    // Revenue (sum of finalAmount for those orders)
    private double totalRevenue;
}

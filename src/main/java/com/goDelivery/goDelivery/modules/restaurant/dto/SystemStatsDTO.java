package com.goDelivery.goDelivery.modules.restaurant.dto;

import com.goDelivery.goDelivery.shared.enums.StatsPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatsDTO {

    private StatsPeriod period;
    private long totalRestaurants;
    private long approvedRestaurants;
    private long pendingRestaurants;
    private long restaurantStaff;
    private long branchStaff;
    private long totalStaffMembers;
    private long totalOrders;
    private double totalRevenue;
}

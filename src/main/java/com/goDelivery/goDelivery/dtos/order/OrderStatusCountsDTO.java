package com.goDelivery.goDelivery.dtos.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusCountsDTO {

    private long totalOrders;
    private long placedOrders;
    private long confirmedOrders;
    private long paidOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private long pendingOrders;
}

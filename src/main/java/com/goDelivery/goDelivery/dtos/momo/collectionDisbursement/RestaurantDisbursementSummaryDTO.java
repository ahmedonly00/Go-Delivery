package com.goDelivery.goDelivery.dtos.momo.collectionDisbursement;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDisbursementSummaryDTO {

    private Long restaurantId;
    private String restaurantName;
    private BigDecimal totalDisbursed;
    private BigDecimal totalCommission;
    private Long totalTransactions;
    private List<DisbursementSummaryDTO> transactions;
    
}

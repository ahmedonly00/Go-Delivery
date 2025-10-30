package com.goDelivery.goDelivery.dtos.restaurant;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantApprovalRequest {
    
    @NotNull(message = "Approval decision is required")
    private Boolean approved; // true for approve, false for reject
    
    private String rejectionReason; // Required if approved = false
}

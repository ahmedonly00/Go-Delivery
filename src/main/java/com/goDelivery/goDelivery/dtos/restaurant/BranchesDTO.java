package com.goDelivery.goDelivery.dtos.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchesDTO {
    private Long branchId;
    private String branchName;
    private String address;
    private Float latitude;
    private Float longitude;
    private String phoneNumber;
    private String operatingHours;
    private boolean isActive;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Long restaurantId;
}

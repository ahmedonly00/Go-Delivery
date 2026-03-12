package com.goDelivery.goDelivery.modules.restaurant.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryDTO {

    private Long categoryId;
    private String categoryName;
    private boolean isActive;
    private LocalDate createdAt;
    private Long branchId;
    private Long restaurantId;
}

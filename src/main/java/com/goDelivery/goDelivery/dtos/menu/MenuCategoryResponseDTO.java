package com.goDelivery.goDelivery.dtos.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryResponseDTO {
    private Long categoryId;
    private String categoryName;
    private LocalDate createdAt;
    private Long restaurantId;
}

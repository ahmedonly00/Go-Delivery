package com.goDelivery.goDelivery.dtos.menu;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String menuItemName;
    private String description;
    private Float price;
    private String image;
    private String ingredients;
    private boolean isAvailable;
    private Integer preparationTime;
    private Integer preparationScore;
    private Long restaurantId;
    private Long categoryId;
    private String categoryName;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}

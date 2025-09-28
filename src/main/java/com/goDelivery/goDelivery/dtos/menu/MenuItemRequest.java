package com.goDelivery.goDelivery.dtos.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuItemRequest {
    @NotBlank(message = "Menu item name is required")
    private String menuItemName;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Float price;

    private String image;
    private String ingredients;
    private boolean isAvailable = true;
    
    @Positive(message = "Preparation time must be greater than 0")
    private Integer preparationTime;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
}

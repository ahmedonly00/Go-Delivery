package com.goDelivery.goDelivery.dtos.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MenuItemDTO {
    private Long itemId;  // Changed from id to itemId
    
    @NotBlank(message = "Menu item name is required")
    private String itemName;  // Changed from name to itemName
    
    private String description;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be a positive number")
    private Float price;  // Changed from Double to Float
    
    private String imageUrl;
    
    private Long categoryId;
    
    private Long branchId;
    
    private boolean available = true;
        
    @Positive(message = "Preparation time must be a positive number")
    private Integer preparationTime; // in minutes
}

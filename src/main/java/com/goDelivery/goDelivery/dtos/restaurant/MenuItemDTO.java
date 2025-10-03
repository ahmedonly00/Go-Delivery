package com.goDelivery.goDelivery.dtos.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MenuItemDTO {
    private Long id;
    
    @NotBlank(message = "Menu item name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be a positive number")
    private Double price;
    
    private String imageUrl;
    
    private String category;
    
    private boolean available = true;
    
    @Positive(message = "Preparation time must be a positive number")
    private Integer preparationTime; // in minutes
}

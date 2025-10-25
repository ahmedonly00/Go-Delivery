package com.goDelivery.goDelivery.dtos.menucategory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryFromTemplateRequest {
    
    @NotNull(message = "Template ID is required")
    private Long templateId;
    
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
    
    private String customImage; // Optional: override default image
}

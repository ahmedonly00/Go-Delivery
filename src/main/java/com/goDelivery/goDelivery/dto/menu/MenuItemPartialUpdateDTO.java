package com.goDelivery.goDelivery.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemPartialUpdateDTO {
    private Float price;
    private Boolean isAvailable;
    private String description;
    private String ingredients;
    private Integer preparationTime;
    
    // Field to track which property is being updated
    private String updateField;
}

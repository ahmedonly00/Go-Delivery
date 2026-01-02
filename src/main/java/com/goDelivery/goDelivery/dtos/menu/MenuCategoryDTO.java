package com.goDelivery.goDelivery.dtos.menu;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.goDelivery.goDelivery.model.MenuItem;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.model.Branches;

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
    
    private String description;
    
    private String imageUrl;
    
    private boolean isActive;

    private LocalDate createdAt;
    
    private Long branchId;
    
    private Long restaurantId;

    private Restaurant restaurant;
    
    private Branches branch;

    @Builder.Default
    private List<MenuItem> menuItems = new ArrayList<>();
}

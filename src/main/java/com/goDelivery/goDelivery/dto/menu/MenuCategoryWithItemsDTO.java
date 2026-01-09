package com.goDelivery.goDelivery.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuCategoryWithItemsDTO {
    private Long categoryId;
    private String categoryName;
    private List<MenuItemDTO> items;
    private boolean isInherited;
    private int itemCount;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MenuItemDTO {
        private Long menuItemId;
        private String menuItemName;
        private String description;
        private Float price;
        private String image;
        private String ingredients;
        private boolean isAvailable;
        private Integer preparationTime;
        private boolean isInherited;
    }
}

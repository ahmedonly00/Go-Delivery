package com.goDelivery.goDelivery.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemUpdateDTO {
    private Long menuItemId;
    private String menuItemName;
    private Float price;
    private boolean isAvailable;
    private Long categoryId;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String updateType; // "PRICE_CHANGE", "AVAILABILITY_CHANGE", "GENERAL_UPDATE"
}

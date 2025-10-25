package com.goDelivery.goDelivery.dtos.menucategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuCategoryTemplateResponse {
    private Long templateId;
    private String categoryName;
    private String description;
    private String defaultImageUrl;
    private Integer sortOrder;
    private boolean isActive;
}

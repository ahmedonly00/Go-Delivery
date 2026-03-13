package com.goDelivery.goDelivery.modules.branch.dto;

import com.goDelivery.goDelivery.modules.restaurant.dto.MenuItemRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private boolean success;
    private String message;
    private String fileUrl;
    private List<MenuItemRequest> menuItems;
}

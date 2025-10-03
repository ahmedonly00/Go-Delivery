package com.goDelivery.goDelivery.dtos.file;

import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
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
    private List<MenuItemRequest> menuItems;
    private String fileUrl;
}

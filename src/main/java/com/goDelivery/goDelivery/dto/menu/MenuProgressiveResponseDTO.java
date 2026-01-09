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
public class MenuProgressiveResponseDTO {
    private List<MenuCategoryWithItemsDTO> categories;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private boolean hasMore;
    private String nextCursor;
    private String prevCursor;
}

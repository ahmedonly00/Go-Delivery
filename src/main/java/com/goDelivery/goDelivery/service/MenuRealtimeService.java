package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dto.menu.MenuItemUpdateDTO;
import com.goDelivery.goDelivery.model.MenuItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuRealtimeService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastMenuItemUpdate(Long branchId, MenuItem menuItem, String updatedBy) {
        MenuItemUpdateDTO update = MenuItemUpdateDTO.builder()
                .menuItemId(menuItem.getMenuItemId())
                .menuItemName(menuItem.getMenuItemName())
                .price(menuItem.getPrice())
                .isAvailable(menuItem.isAvailable())
                .categoryId(menuItem.getCategory().getCategoryId())
                .updatedBy(updatedBy)
                .updatedAt(LocalDateTime.now())
                .build();

        // Send to branch-specific topic
        messagingTemplate.convertAndSend("/topic/branch/" + branchId + "/menu", update);
        
        log.info("Broadcasted menu item update for branch {}: {}", branchId, menuItem.getMenuItemName());
    }

    public void broadcastMenuItemAvailability(Long branchId, Long menuItemId, boolean isAvailable, String updatedBy) {
        Map<String, Object> update = new HashMap<>();
        update.put("menuItemId", menuItemId);
        update.put("isAvailable", isAvailable);
        update.put("updatedBy", updatedBy);
        update.put("timestamp", LocalDateTime.now());
        update.put("type", "AVAILABILITY_CHANGE");

        messagingTemplate.convertAndSend("/topic/branch/" + branchId + "/menu", update);
        log.info("Broadcasted availability change for item {} in branch {}", menuItemId, branchId);
    }

    public void broadcastMenuItemPriceChange(Long branchId, Long menuItemId, Float oldPrice, Float newPrice, String updatedBy) {
        Map<String, Object> update = new HashMap<>();
        update.put("menuItemId", menuItemId);
        update.put("oldPrice", oldPrice);
        update.put("newPrice", newPrice);
        update.put("updatedBy", updatedBy);
        update.put("timestamp", LocalDateTime.now());
        update.put("type", "PRICE_CHANGE");

        messagingTemplate.convertAndSend("/topic/branch/" + branchId + "/menu", update);
        log.info("Broadcasted price change for item {} in branch {}: {} -> {}", 
                menuItemId, branchId, oldPrice, newPrice);
    }

    public void broadcastMenuItemAdded(Long branchId, MenuItem menuItem, String addedBy) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "ITEM_ADDED");
        update.put("menuItem", menuItem);
        update.put("addedBy", addedBy);
        update.put("timestamp", LocalDateTime.now());

        messagingTemplate.convertAndSend("/topic/branch/" + branchId + "/menu", update);
        log.info("Broadcasted new menu item for branch {}: {}", branchId, menuItem.getMenuItemName());
    }

    public void broadcastMenuItemRemoved(Long branchId, Long menuItemId, String removedBy) {
        Map<String, Object> update = new HashMap<>();
        update.put("menuItemId", menuItemId);
        update.put("type", "ITEM_REMOVED");
        update.put("removedBy", removedBy);
        update.put("timestamp", LocalDateTime.now());

        messagingTemplate.convertAndSend("/topic/branch/" + branchId + "/menu", update);
        log.info("Broadcasted menu item removal for branch {}: item {}", branchId, menuItemId);
    }
}

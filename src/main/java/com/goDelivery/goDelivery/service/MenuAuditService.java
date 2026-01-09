package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.model.MenuAuditLog;
import com.goDelivery.goDelivery.repository.MenuAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuAuditService {

    private final MenuAuditLogRepository auditLogRepository;
    private final UsersService usersService;

    @Transactional
    public void logMenuChange(String entityType, Long entityId, Long branchId, Long restaurantId,
                             String action, String fieldName, String oldValue, String newValue,
                             String reason, HttpServletRequest request) {
        try {
            String userEmail = getCurrentUserEmail();
            String userRole = getCurrentUserRole();
            
            MenuAuditLog auditLog = MenuAuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .branchId(branchId)
                    .restaurantId(restaurantId)
                    .action(action)
                    .fieldName(fieldName)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .userEmail(userEmail)
                    .userRole(userRole)
                    .createdAt(LocalDate.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .reason(reason)
                    .build();
            
            auditLogRepository.save(auditLog);
            log.info("Menu audit logged: {} {} by {}", action, entityType, userEmail);
        } catch (Exception e) {
            log.error("Failed to log menu audit", e);
            // Don't throw exception to avoid breaking main flow
        }
    }

    @Transactional
    public void logMenuItemUpdate(Long menuItemId, Long branchId, String fieldName, 
                                String oldValue, String newValue, String reason, HttpServletRequest request) {
        logMenuChange("MENU_ITEM", menuItemId, branchId, null, "UPDATE", fieldName, 
                     oldValue, newValue, reason, request);
    }

    @Transactional
    public void logMenuItemCreate(Long menuItemId, Long branchId, String itemName, HttpServletRequest request) {
        logMenuChange("MENU_ITEM", menuItemId, branchId, null, "CREATE", "itemName", 
                     null, itemName, null, request);
    }

    @Transactional
    public void logMenuItemDelete(Long menuItemId, Long branchId, String itemName, HttpServletRequest request) {
        logMenuChange("MENU_ITEM", menuItemId, branchId, null, "DELETE", "itemName", 
                     itemName, null, null, request);
    }

    @Transactional
    public void logCategoryCreate(Long categoryId, Long branchId, String categoryName, HttpServletRequest request) {
        logMenuChange("MENU_CATEGORY", categoryId, branchId, null, "CREATE", "categoryName", 
                     null, categoryName, null, request);
    }

    private String getCurrentUserEmail() {
        try {
            return usersService.getCurrentUser().getEmail();
        } catch (Exception e) {
            return "system";
        }
    }

    private String getCurrentUserRole() {
        try {
            return usersService.getCurrentUser().getRole().toString();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) return null;
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}

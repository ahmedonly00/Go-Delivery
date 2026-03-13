package com.goDelivery.goDelivery.modules.restaurant.dto;

import com.goDelivery.goDelivery.shared.enums.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantUserResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Roles role;
    private String permissions;
    private boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long restaurantId;
    private String restaurantName;
}

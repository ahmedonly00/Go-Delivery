package com.goDelivery.goDelivery.dtos.user;

import com.goDelivery.goDelivery.Enum.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantUserResponse {
    private Long userId;
    private String fullNames;
    private String email;
    private String phoneNumber;
    private Roles role;
    private String permissions;
    private boolean isActive;
    private LocalDate lastLogin;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    
    // Related entities
    private Long restaurantId;
    private String restaurantName;
    private Long applicationId;  // ID of the related application, if any
}

package com.goDelivery.goDelivery.dtos.profile;

import com.goDelivery.goDelivery.Enum.Roles;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProfileResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Roles role;
    private String userType; // "SUPER_ADMIN", "RESTAURANT_USER", "BRANCH_USER", "CUSTOMER"
    private boolean emailVerified;
    private boolean isActive;
    private LocalDate createdAt;

    // Customer-specific
    private String address;

    // Restaurant/Branch user specific
    private Long restaurantId;
    private String restaurantName;
    private Long branchId;
    private String branchName;
}

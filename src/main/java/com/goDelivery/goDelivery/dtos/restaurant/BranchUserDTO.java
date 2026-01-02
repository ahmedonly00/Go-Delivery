package com.goDelivery.goDelivery.dtos.restaurant;

import com.goDelivery.goDelivery.Enum.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchUserDTO {
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String password;
    private Roles role;
    private String permissions;
    private boolean emailVerified;
    private boolean isActive;
    private boolean setupComplete;
    private Long branchId;
    private String branchName;
    private Long restaurantId;
    private String restaurantName;
}

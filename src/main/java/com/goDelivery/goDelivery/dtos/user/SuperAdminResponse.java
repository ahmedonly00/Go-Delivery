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
public class SuperAdminResponse {
    private Long adminId;
    private String fullNames;
    private String email;
    private Roles role;
    private boolean isActive;
    private LocalDate lastLogin;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}

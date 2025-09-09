package com.goDelivery.goDelivery.dtos.user;

import com.goDelivery.goDelivery.Enum.Roles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class SuperAdminRequest {
    @NotBlank(message = "Full name is required")
    private String fullNames;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @NotNull(message = "Role is required")
    private Roles role;
    
    private boolean isActive = true;
}

package com.goDelivery.goDelivery.dto.auth;

import com.goDelivery.goDelivery.Enum.Roles;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class BranchUserRegistrationDTO {
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    private Roles role = Roles.BRANCH_MANAGER;
    
    @NotBlank(message = "Permissions are required")
    private String permissions = "ORDER_MANAGEMENT,MENU_MANAGEMENT,BRANCH_SETTINGS";
}

package com.goDelivery.goDelivery.dtos.user;

import com.goDelivery.goDelivery.Enum.Roles;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RestaurantUserRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[0-9]{1,5}[-\\s\\.]?[0-9]{1,6}$", message = "Phone number must be 10-15 digits and may include country code (e.g., +258 84 123 4567, 258-84-123-4567)")
    private String phoneNumber;

    // Role is optional - defaults to CASHIER if not provided
    private Roles role;

    // Permissions are optional - defaults based on role
    private String permissions;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    private boolean isActive = true;
}

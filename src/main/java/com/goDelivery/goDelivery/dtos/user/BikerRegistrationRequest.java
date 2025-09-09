package com.goDelivery.goDelivery.dtos.user;

import com.goDelivery.goDelivery.Enum.VehicleType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BikerRegistrationRequest {
    @NotBlank(message = "Full name is required")
    private String fullNames;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number should be valid")
    private String phoneNumber;
    
    @NotBlank(message = "National ID is required")
    private String nationalId;
    
    @NotBlank(message = "License number is required")
    private String licenseNumber;
    
    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;
    
    @NotBlank(message = "Vehicle plate is required")
    private String vehiclePlate;
    
    @NotBlank(message = "Vehicle model is required")
    private String vehicleModel;
    
    @NotBlank(message = "Profile image is required")
    private String profileImage;
}

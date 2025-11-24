package com.goDelivery.goDelivery.dtos.biker;

import com.goDelivery.goDelivery.Enum.VehicleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BikerUpdateRequest {
    @NotBlank(message = "Full name is required")
    private String fullNames;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number should be between 10-15 digits")
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
    
    private String profileImage;
    
    private boolean isActive;
}

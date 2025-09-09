package com.goDelivery.goDelivery.dtos.restaurant;

import com.goDelivery.goDelivery.Enum.ApplicationStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RestaurantApplicationRequest {
    @NotBlank(message = "Business name is required")
    private String businessName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    // Admin fields (for updates)
    private ApplicationStatus applicationStatus;
    private String rejectionReason;
    private LocalDate reviewedAt;
    private LocalDate approvedAt;
    private Long reviewedById;  // ID of the admin who reviewed the application
}

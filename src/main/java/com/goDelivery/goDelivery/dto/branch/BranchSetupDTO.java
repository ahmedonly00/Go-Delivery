package com.goDelivery.goDelivery.dto.branch;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class BranchSetupDTO {
    
    @NotBlank(message = "Branch name is required")
    @Size(min = 2, max = 100, message = "Branch name must be between 2 and 100 characters")
    private String branchName;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    private Float latitude;
    
    private Float longitude;
    
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    @NotBlank(message = "Operating hours are required")
    private String operatingHours;
    
    private String description;
    
    private String contactEmail;
    
    private String deliveryRadius; // in km
    
    private String specialInstructions;
    
    // Initial menu categories to create
    private java.util.List<String> initialMenuCategories;
}

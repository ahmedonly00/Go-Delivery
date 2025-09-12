package com.goDelivery.goDelivery.dtos.restaurant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRestaurantApplicationRequest {
    @NotBlank(message = "Business name is required")
    private String businessName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotBlank(message = "Owner name is required")
    private String ownerName;
    
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    @NotBlank(message = "Cuisine type is required")
    private String cuisineType;
    
    private Integer yearsInBusiness;
    
    private String description;
}

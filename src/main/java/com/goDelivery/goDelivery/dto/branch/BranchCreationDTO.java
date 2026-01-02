package com.goDelivery.goDelivery.dto.branch;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import java.time.LocalTime;
import java.util.List;

@Data
public class BranchCreationDTO {
    
    @NotBlank(message = "Branch name is required")
    @Size(min = 2, max = 100, message = "Branch name must be between 2 and 100 characters")
    private String branchName;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    private String addressLine2;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "State/Province is required")
    private String state;
    
    @NotBlank(message = "Postal code is required")
    private String postalCode;
    
    @NotBlank(message = "Country is required")
    private String country;
    
    private Float latitude;
    
    private Float longitude;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be valid")
    private String phoneNumber;
    
    @Email(message = "Email must be valid")
    private String email;
    
    private String website;
    
    private String description;
    
    // Operating hours
    private LocalTime mondayOpen;
    private LocalTime mondayClose;
    private LocalTime tuesdayOpen;
    private LocalTime tuesdayClose;
    private LocalTime wednesdayOpen;
    private LocalTime wednesdayClose;
    private LocalTime thursdayOpen;
    private LocalTime thursdayClose;
    private LocalTime fridayOpen;
    private LocalTime fridayClose;
    private LocalTime saturdayOpen;
    private LocalTime saturdayClose;
    private LocalTime sundayOpen;
    private LocalTime sundayClose;
    
    // Delivery settings
    private Boolean deliveryAvailable = false;
    private Float deliveryRadius; // in km
    private Float minimumOrderAmount;
    private Float deliveryFee;
    
    // Social media
    private String facebookUrl;
    private String instagramUrl;
    private String twitterUrl;
    
    // Branch manager details
    @NotBlank(message = "Manager name is required")
    private String managerName;
    
    @NotBlank(message = "Manager email is required")
    @Email(message = "Manager email must be valid")
    private String managerEmail;
    
    @NotBlank(message = "Manager phone is required")
    private String managerPhone;
    
    private String managerPassword;
    
    // Initial menu categories
    private List<String> initialMenuCategories;
    
    // Tags for search
    private List<String> tags;
    
    // Special features
    private Boolean hasParking;
    private Boolean hasWifi;
    private Boolean hasOutdoorSeating;
    private Boolean acceptsReservations;
    
    // Average rating and review count (for display)
    private Double averageRating = 0.0;
    private Integer reviewCount = 0;
}

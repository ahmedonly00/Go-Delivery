package com.goDelivery.goDelivery.dtos.restaurant;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import com.goDelivery.goDelivery.Enum.DeliveryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDTO {
    private Long restaurantId;
    
    @NotBlank(message = "Restaurant name is required")
    private String restaurantName;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotBlank(message = "Cuisine type is required")
    private String cuisineType;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid")
    private String phoneNumber;
    
    private String logoUrl;

    @NotBlank(message = "Description is required")
    private String description;

    @Builder.Default
    private Double rating = 0.0;
    
    private DeliveryType deliveryType;

    private Float deliveryFee;

    private Double deliveryRadius;
        
    @JsonIgnore
    private MultipartFile logoFile;
    
    @Builder.Default
    private Integer totalReviews = 0;
    
    @PositiveOrZero(message = "Average preparation time must be zero or positive")
    private Integer averagePreparationTime;
    
    @NotNull(message = "Minimum order amount is required")
    @PositiveOrZero(message = "Minimum order amount must be zero or positive")
    private Float minimumOrderAmount;

    // Business Documents
    private String commercialRegistrationCertificateUrl;
    
    private String taxIdentificationNumber;
    
    private String taxIdentificationDocumentUrl;
    
    // Approval fields
    @Builder.Default
    private Boolean isApproved = false;
    
    private com.goDelivery.goDelivery.Enum.ApprovalStatus approvalStatus;
    
    private String rejectionReason;
    
    private String reviewedBy;
    
    private LocalDate reviewedAt;

    private OperatingHoursDTO operatingHours;
    
    @Builder.Default
    private boolean isActive = true;

    private LocalDate createdAt;
    private LocalDate updatedAt;
    
}

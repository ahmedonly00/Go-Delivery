package com.goDelivery.goDelivery.dtos.restaurant;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import com.goDelivery.goDelivery.Enum.ApprovalStatus;
import com.goDelivery.goDelivery.Enum.DeliveryType;
import com.goDelivery.goDelivery.Enum.DistanceUnit;
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
    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[0-9]{1,5}[-\\s\\.]?[0-9]{1,6}$", message = "Phone number must be 10-15 digits and may include country code (e.g., +258 84 123 4567, 258-84-123-4567)")
    private String phoneNumber;

    private String logoUrl;

    @NotBlank(message = "Description is required")
    private String description;

    @Builder.Default
    private Double rating = 0.0;

    private DeliveryType deliveryType;

    private Float deliveryFee; // Legacy field for backward compatibility

    private Double deliveryRadius;

    private DistanceUnit radiusUnit;

    private Float baseDeliveryFee;

    private Float perKmFee;

    // Location-based fields (populated when searching by location)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double distanceFromUser; // In kilometers

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String distanceDisplay; // e.g., "2.5 km" or "500 m"

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer estimatedDeliveryMinutes; // ETA for delivery

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

    private ApprovalStatus approvalStatus;

    private String rejectionReason;

    private String reviewedBy;

    private LocalDate reviewedAt;

    private OperatingHoursDTO operatingHours;

    @Builder.Default
    private boolean isActive = true;

    private LocalDate createdAt;
    private LocalDate updatedAt;

}

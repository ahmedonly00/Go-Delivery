package com.goDelivery.goDelivery.dtos.restaurant;

import com.goDelivery.goDelivery.Enum.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchesDTO {
    private Long branchId;
    private String branchName;
    private String address;
    private Float latitude;
    private Float longitude;
    private String phoneNumber;
    private String email;
    private String website;
    private String operatingHours;
    private boolean isActive;
    private String logoUrl;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private ApprovalStatus approvalStatus;
    private String businessDocumentUrl;
    private String operatingLicenseUrl;
    private String description;
    private String approvedBy;
    private LocalDate approvedAt;
    private String reviewedBy;
    private LocalDate reviewedAt;
    private String rejectionReason;
    private Long restaurantId;
    
    // Additional fields for comprehensive branch info
    private Boolean deliveryAvailable;
    private Float deliveryRadius;
    private Float minimumOrderAmount;
    private Float deliveryFee;
    private String facebookUrl;
    private String instagramUrl;
    private String twitterUrl;
    private Boolean hasParking;
    private Boolean hasWifi;
    private Boolean hasOutdoorSeating;
    private Boolean acceptsReservations;
    private Double averageRating;
    private Integer reviewCount;
}

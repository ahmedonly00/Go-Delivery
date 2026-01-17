package com.goDelivery.goDelivery.dtos.restaurant;

import com.goDelivery.goDelivery.Enum.ApprovalStatus;
import com.goDelivery.goDelivery.Enum.BranchSetupStatus;
import com.goDelivery.goDelivery.Enum.DeliveryType;
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
    private BranchSetupStatus setupStatus;
    private DeliveryType deliveryType;
    private Integer averagePreparationTime;
    private boolean isActive;
    private String logoUrl;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private ApprovalStatus approvalStatus;
    private String  commercialRegistrationCertificateUrl;
    private String  taxIdentificationDocumentUrl;
    private String taxIdentificationNumber;
    private String description;
    private String approvedBy;
    private LocalDate approvedAt;
    private String reviewedBy;
    private LocalDate reviewedAt;
    private String rejectionReason;
    private Long restaurantId;
    
    // Additional fields for comprehensive branch info
    private Boolean deliveryAvailable;
    private Double deliveryRadius;
    private Float minimumOrderAmount;
    private Float deliveryFee;
    private Double averageRating;
    private Integer reviewCount;
}

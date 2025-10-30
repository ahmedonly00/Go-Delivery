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
public class RestaurantReviewDTO {
    
    private Long restaurantId;
    private String restaurantName;
    private String location;
    private String cuisineType;
    private String email;
    private String phoneNumber;
    private String logoUrl;
    private String description;
    
    // Business Details
    private Float deliveryFee;
    private Float minimumOrderAmount;
    private Integer averagePreparationTime;
    
    // Business Documents - All visible for review
    private String commercialRegistrationCertificateUrl;
    private String taxIdentificationNumber;
    private String taxIdentificationDocumentUrl;
    private String businessOperatingLicenseUrl;
    
    // Approval Information
    private Boolean isApproved;
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
    private String reviewedBy;
    private LocalDate reviewedAt;
    
    // Metadata
    private LocalDate createdAt;
    private LocalDate updatedAt;
    
    // Helper method to check if all documents are uploaded
    public boolean hasAllDocuments() {
        return commercialRegistrationCertificateUrl != null &&
               taxIdentificationDocumentUrl != null &&
               businessOperatingLicenseUrl != null &&
               taxIdentificationNumber != null;
    }
    
    // Helper method to count uploaded documents
    public int getUploadedDocumentsCount() {
        int count = 0;
        if (commercialRegistrationCertificateUrl != null) count++;
        if (taxIdentificationDocumentUrl != null) count++;
        if (businessOperatingLicenseUrl != null) count++;
        return count;
    }
}

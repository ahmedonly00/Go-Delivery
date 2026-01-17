package com.goDelivery.goDelivery.dto.branch;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.goDelivery.goDelivery.Enum.DeliveryType;
import com.goDelivery.goDelivery.dtos.restaurant.OperatingHoursDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchManagerSetupDTO {
    
    private String branchName;
    
    private String address;
    
    private String cuisineType;
    
    @Email(message = "Email must be valid")
    private String email;
    
    @NotEmpty(message = "Phone number must be valid")
    private String phoneNumber;
    
    private String logoUrl;
    
    private String description;
    
    private DeliveryType deliveryType;
    
    private Float deliveryFee;
    
    private Double deliveryRadius;
    
    @PositiveOrZero(message = "Average preparation time must be zero or positive")
    private Integer averagePreparationTime;

    private Boolean deliveryAvailable;
    
    @PositiveOrZero(message = "Minimum order amount must be zero or positive")
    private Float minimumOrderAmount;

    // Document URLs (for display)
    private String commercialRegistrationCertificateUrl;
    private String taxIdentificationDocumentUrl;
    private String taxIdentificationNumber;
    
    // File uploads (not stored in DB)
    @JsonIgnore
    @Schema(hidden = true)
    private MultipartFile commercialRegistrationFile;
    
    @JsonIgnore
    @Schema(hidden = true)
    private MultipartFile taxIdentificationFile;
    
    private OperatingHoursDTO operatingHours;

    public MultipartFile getCommercialRegistrationFile() {
        return commercialRegistrationFile;
    }
    public void setCommercialRegistrationFile(MultipartFile commercialRegistrationFile) {
        this.commercialRegistrationFile = commercialRegistrationFile;
    }
    public MultipartFile getTaxIdentificationFile() {
        return taxIdentificationFile;
    }
    public void setTaxIdentificationFile(MultipartFile taxIdentificationFile) {
        this.taxIdentificationFile = taxIdentificationFile;
    }
}

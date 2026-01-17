package com.goDelivery.goDelivery.dto.branch;

import org.springframework.web.multipart.MultipartFile;

import com.goDelivery.goDelivery.Enum.DeliveryType;
import com.goDelivery.goDelivery.dtos.restaurant.OperatingHoursDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchManagerSetupDTO {
    
    private String branchName;
    
    private String location;
    
    private String cuisineType;
    
    @Email(message = "Email must be valid")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be valid")
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
    
    private MultipartFile  commercialRegistrationCertificateUrl;

    private MultipartFile  taxIdentificationDocumentUrl;
    
    private String taxIdentificationNumber;
    
    private OperatingHoursDTO operatingHours;
}

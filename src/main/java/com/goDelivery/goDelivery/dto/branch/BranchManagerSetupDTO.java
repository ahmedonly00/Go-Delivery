package com.goDelivery.goDelivery.dto.branch;

import com.goDelivery.goDelivery.Enum.DeliveryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import java.time.LocalTime;

/**
 * DTO for Branch Manager to complete branch setup in a single API call.
 * All fields are optional - branch manager only fills in what wasn't set by restaurant admin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchManagerSetupDTO {

    // ==================== BASIC INFO ====================
    
    private String branchName;
    
    private String address;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be valid")
    private String phoneNumber;
    
    @Email(message = "Email must be valid")
    private String email;
    
    private String description;

    // ==================== LOCATION ====================
    
    private Float latitude;
    
    private Float longitude;

    // ==================== BRANDING ====================
    
    private String logoUrl;
    
    private String cuisineType;

    // ==================== DELIVERY SETTINGS ====================
    
    private DeliveryType deliveryType;
    
    private Float deliveryFee;
    
    private Float minimumOrderAmount;
    
    private Double deliveryRadius;
    
    private Integer averagePreparationTime;
    
    private Boolean deliveryAvailable;

    // ==================== OPERATING HOURS ====================
    
    private LocalTime mondayOpen;
    private LocalTime mondayClose;
    private Boolean mondayClosed;
    
    private LocalTime tuesdayOpen;
    private LocalTime tuesdayClose;
    private Boolean tuesdayClosed;
    
    private LocalTime wednesdayOpen;
    private LocalTime wednesdayClose;
    private Boolean wednesdayClosed;
    
    private LocalTime thursdayOpen;
    private LocalTime thursdayClose;
    private Boolean thursdayClosed;
    
    private LocalTime fridayOpen;
    private LocalTime fridayClose;
    private Boolean fridayClosed;
    
    private LocalTime saturdayOpen;
    private LocalTime saturdayClose;
    private Boolean saturdayClosed;
    
    private LocalTime sundayOpen;
    private LocalTime sundayClose;
    private Boolean sundayClosed;

    // ==================== DOCUMENTS (if not uploaded by admin) ====================
    
    private String businessDocumentUrl;
    
    private String operatingLicenseUrl;
    
    private String taxIdentificationNumber;
}

package com.goDelivery.goDelivery.dto.branch;

import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.dto.auth.BranchUserRegistrationDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BranchRegistrationDTO extends BranchesDTO {
    private BranchUserRegistrationDTO branchManager;
    private String restaurantAdminEmail; // Email of the restaurant admin approving this
    private String businessDocumentUrl;
    private String operatingLicenseUrl;
    private List<String> menuCategoryNames; // Initial menu categories to create
    private String description;
    private boolean requiresApproval = true; // Branches may need approval
}

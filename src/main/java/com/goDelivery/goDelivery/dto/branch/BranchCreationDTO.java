package com.goDelivery.goDelivery.dto.branch;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

@Data
public class BranchCreationDTO {
    
    @NotBlank(message = "Branch name is required")
    @Size(min = 2, max = 100, message = "Branch name must be between 2 and 100 characters")
    private String branchName;
    
    @NotBlank(message = "Address is required")
    private String location;
        
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "State/Province is required")
    private String state;
        
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be valid")
    private String phoneNumber;
    
    @Email(message = "Email must be valid")
    private String email;
        
    private String description;
    
    // Branch manager details
    @NotBlank(message = "Manager name is required")
    private String managerName;
    
    @NotBlank(message = "Manager email is required")
    @Email(message = "Manager email must be valid")
    private String managerEmail;
    
    @NotBlank(message = "Manager phone is required")
    private String managerPhone;
    
    private String managerPassword;
}

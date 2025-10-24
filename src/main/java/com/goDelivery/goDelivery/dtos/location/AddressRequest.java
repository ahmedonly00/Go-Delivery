package com.goDelivery.goDelivery.dtos.location;

import com.goDelivery.goDelivery.Enum.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequest {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "City ID is required")
    private Long cityId;
    
    @NotBlank(message = "Street is required")
    private String street;
    
    @NotBlank(message = "Area name is required")
    private String areaName;
    
    @NotBlank(message = "House number is required")
    private String houseNumber;
    
    @NotBlank(message = "Local contact number is required")
    private String localContactNumber;
    
    @NotNull(message = "Latitude is required")
    private Float latitude;
    
    @NotNull(message = "Longitude is required")
    private Float longitude;
    
    @NotNull(message = "Address type is required")
    private AddressType addressType;
    
    @NotBlank(message = "Usage option is required")
    private String usageOption;
    
    @Builder.Default
    private Boolean isDefault = false;
}

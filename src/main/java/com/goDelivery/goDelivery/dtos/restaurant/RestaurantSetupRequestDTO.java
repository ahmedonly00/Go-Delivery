package com.goDelivery.goDelivery.dtos.restaurant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class RestaurantSetupRequestDTO {

    @NotNull(message = "Basic information is required")
    @Valid
    private RestaurantBasicInfoDTO basicInfo;

    @NotNull(message = "Location information is required")
    @Valid
    private RestaurantLocationDTO location;

    @NotNull(message = "Operating hours are required")
    @Valid
    private Map<String, OperatingHoursDTO> operatingHours;

    @Valid
    private RestaurantBrandingDTO branding;

    @Valid
    private RestaurantSettingsDTO settings;
}

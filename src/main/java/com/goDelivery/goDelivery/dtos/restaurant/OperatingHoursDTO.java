package com.goDelivery.goDelivery.dtos.restaurant;

import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class OperatingHoursDTO {
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format. Use HH:MM")
    private String open;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format. Use HH:MM")
    private String close;
    
    private boolean isClosed = false;
}

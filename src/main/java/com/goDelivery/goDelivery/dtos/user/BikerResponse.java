package com.goDelivery.goDelivery.dtos.user;

import com.goDelivery.goDelivery.Enum.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BikerResponse {
    private Long bikerId;
    private String fullNames;
    private String email;
    private String phoneNumber;
    private String nationalId;
    private String licenseNumber;
    private VehicleType vehicleType;
    private String vehiclePlate;
    private String vehicleModel;
    private String profileImage;
    private Float rating;
    private Integer totalDeliveries;
    private Integer successfulDeliveries;
    private Float currentLatitude;
    private Float currentLongitude;
    private boolean isAvailable;
    private boolean isOnline;
    private boolean isActive;
    private LocalDate joinedAt;
    private LocalDate lastActive;
    
    // Derived fields
    private Double completionRate; // calculated as (successfulDeliveries / totalDeliveries) * 100
}

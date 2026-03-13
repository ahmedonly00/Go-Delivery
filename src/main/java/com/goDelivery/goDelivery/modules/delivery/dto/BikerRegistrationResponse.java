package com.goDelivery.goDelivery.modules.delivery.dto;

import com.goDelivery.goDelivery.shared.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BikerRegistrationResponse {
    private Long bikerId;
    private String fullNames;
    private String email;
    private String phoneNumber;
    private String licenseNumber;
    private VehicleType vehicleType;
    private String vehiclePlate;
    private String vehicleModel;
    private String profileImage;
    private LocalDate joinedAt;
    private String status;
}

package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.dtos.biker.BikerRegistrationRequest;
import com.goDelivery.goDelivery.dtos.biker.BikerRegistrationResponse;
import com.goDelivery.goDelivery.model.Bikers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class BikerMapper {

    private final PasswordEncoder passwordEncoder;

    public Bikers toBiker(BikerRegistrationRequest request) {
        if (request == null) {
            return null;
        }

        return Bikers.builder()
                .fullNames(request.getFullNames())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .nationalId(request.getNationalId())
                .licenseNumber(request.getLicenseNumber())
                .vehicleType(request.getVehicleType())
                .vehiclePlate(request.getVehiclePlate())
                .vehicleModel(request.getVehicleModel())
                .profileImage(request.getProfileImage())
                .rating(0.0f)
                .totalDeliveries(0)
                .successfulDeliveries(0)
                .currentLatitude(0.0f)
                .currentLongitude(0.0f)
                .isAvailable(false)  // New bikers are not available by default
                .isOnline(false)     // New bikers are offline by default
                .isActive(true)      // Set to false if email verification is required
                .joinedAt(LocalDate.now())
                .lastActive(LocalDate.now())
                .roles(Roles.BIKER)
                .build();
    }

    public com.goDelivery.goDelivery.dtos.biker.BikerDetailsResponse toBikerDetailsResponse(Bikers biker) {
        if (biker == null) {
            return null;
        }

        return com.goDelivery.goDelivery.dtos.biker.BikerDetailsResponse.builder()
                .bikerId(biker.getBikerId())
                .fullNames(biker.getFullNames())
                .email(biker.getEmail())
                .phoneNumber(biker.getPhoneNumber())
                .nationalId(biker.getNationalId())
                .licenseNumber(biker.getLicenseNumber())
                .vehicleType(biker.getVehicleType())
                .vehiclePlate(biker.getVehiclePlate())
                .vehicleModel(biker.getVehicleModel())
                .profileImage(biker.getProfileImage())
                .rating(biker.getRating())
                .totalDeliveries(biker.getTotalDeliveries())
                .successfulDeliveries(biker.getSuccessfulDeliveries())
                .isAvailable(biker.isAvailable())
                .isOnline(biker.isOnline())
                .isActive(biker.isActive())
                .joinedAt(biker.getJoinedAt())
                .lastActive(biker.getLastActive())
                .build();
    }
    
    public BikerRegistrationResponse toBikerResponse(Bikers biker) {
        if (biker == null) {
            return null;
        }

        return BikerRegistrationResponse.builder()
                .bikerId(biker.getBikerId())
                .fullNames(biker.getFullNames())
                .email(biker.getEmail())
                .phoneNumber(biker.getPhoneNumber())
                .licenseNumber(biker.getLicenseNumber())
                .vehicleType(biker.getVehicleType())
                .vehiclePlate(biker.getVehiclePlate())
                .vehicleModel(biker.getVehicleModel())
                .profileImage(biker.getProfileImage())
                .joinedAt(biker.getJoinedAt())
                .status(biker.isActive() ? "ACTIVE" : "PENDING_ACTIVATION")
                .build();
    }
}

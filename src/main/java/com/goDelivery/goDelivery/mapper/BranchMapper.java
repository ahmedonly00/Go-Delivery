package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.dto.branch.BranchCreationDTO;
import com.goDelivery.goDelivery.dto.branch.BranchManagerSetupDTO;
import com.goDelivery.goDelivery.dtos.restaurant.OperatingHoursDTO;
import com.goDelivery.goDelivery.Enum.ApprovalStatus;
import com.goDelivery.goDelivery.Enum.BranchSetupStatus;
import com.goDelivery.goDelivery.Enum.DeliveryType;
import com.goDelivery.goDelivery.model.Branches;
import com.goDelivery.goDelivery.model.OperatingHours;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BranchMapper {

    // ==================== BranchCreationDTO Mappings ====================

    public Branches toBranchForCreate(BranchCreationDTO dto) {
        if (dto == null) {
            return null;
        }

        // Build full address from components
        String fullAddress = buildFullAddress(dto);

        return Branches.builder()
                .branchName(dto.getBranchName())
                .address(fullAddress)
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .description(dto.getDescription())
                .deliveryType(DeliveryType.SYSTEM_DELIVERY)
                .setupStatus(BranchSetupStatus.ACCOUNT_CREATED)
                .approvalStatus(ApprovalStatus.PENDING)
                .isActive(false)
                .deliveryAvailable(false)
                .averageRating(0.0)
                .reviewCount(0)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
    }

    // ==================== BranchManagerSetupDTO Mappings ====================

    public BranchManagerSetupDTO toBranchManagerSetupDTO(Branches branch) {
        if (branch == null) {
            return null;
        }

        BranchManagerSetupDTO dto = BranchManagerSetupDTO.builder()
                .branchName(branch.getBranchName())
                .address(branch.getAddress())
                .cuisineType(branch.getCuisineType())
                .email(branch.getEmail())
                .phoneNumber(branch.getPhoneNumber())
                .logoUrl(branch.getLogoUrl())
                .description(branch.getDescription())
                .deliveryType(branch.getDeliveryType())
                .deliveryFee(branch.getDeliveryFee())
                .deliveryRadius(branch.getDeliveryRadius())
                .averagePreparationTime(branch.getAveragePreparationTime())
                .minimumOrderAmount(branch.getMinimumOrderAmount())
                .commercialRegistrationCertificateUrl(branch.getCommercialRegistrationCertificateUrl())
                .taxIdentificationNumber(branch.getTaxIdentificationNumber())
                .taxIdentificationDocumentUrl(branch.getTaxIdentificationDocumentUrl())
                .build();

        // Map operating hours
        OperatingHours hours = branch.getOperatingHours();
        if (hours != null) {
            OperatingHoursDTO hoursDTO = OperatingHoursDTO.builder()
                    .open(hours.getMondayOpen())
                    .close(hours.getMondayClose())
                    .build();
            dto.setOperatingHours(hoursDTO);
        }

        return dto;
    }

    public void updateBranchFromSetupDTO(Branches branch, BranchManagerSetupDTO dto) {
        if (branch == null || dto == null) {
            return;
        }

        // Basic info - only update if provided
        if (dto.getBranchName() != null) branch.setBranchName(dto.getBranchName());
        if (dto.getCuisineType() != null) branch.setCuisineType(dto.getCuisineType());
        if (dto.getEmail() != null) branch.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) branch.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getLogoUrl() != null) branch.setLogoUrl(dto.getLogoUrl());
        if (dto.getDescription() != null) branch.setDescription(dto.getDescription());

        // Delivery settings
        if (dto.getDeliveryType() != null) branch.setDeliveryType(dto.getDeliveryType());
        if (dto.getDeliveryFee() != null) branch.setDeliveryFee(dto.getDeliveryFee());
        if (dto.getDeliveryRadius() != null) branch.setDeliveryRadius(dto.getDeliveryRadius());
        if (dto.getAveragePreparationTime() != null) branch.setAveragePreparationTime(dto.getAveragePreparationTime());
        if (dto.getMinimumOrderAmount() != null) branch.setMinimumOrderAmount(dto.getMinimumOrderAmount());

        // Documents
        if (dto.getTaxIdentificationNumber() != null) branch.setTaxIdentificationNumber(dto.getTaxIdentificationNumber());

        branch.setUpdatedAt(LocalDate.now());
    }

    
    public void updateOperatingHoursFromSetupDTO(OperatingHours hours, BranchManagerSetupDTO dto) {
        if (hours == null || dto == null || dto.getOperatingHours() == null) {
            return;
        }

        OperatingHoursDTO hoursDTO = dto.getOperatingHours();

        // Apply same hours to all days (as per simple OperatingHoursDTO)
        if (hoursDTO.getOpen() != null) {
            hours.setMondayOpen(hoursDTO.getOpen());
            hours.setTuesdayOpen(hoursDTO.getOpen());
            hours.setWednesdayOpen(hoursDTO.getOpen());
            hours.setThursdayOpen(hoursDTO.getOpen());
            hours.setFridayOpen(hoursDTO.getOpen());
            hours.setSaturdayOpen(hoursDTO.getOpen());
            hours.setSundayOpen(hoursDTO.getOpen());
        }

        if (hoursDTO.getClose() != null) {
            hours.setMondayClose(hoursDTO.getClose());
            hours.setTuesdayClose(hoursDTO.getClose());
            hours.setWednesdayClose(hoursDTO.getClose());
            hours.setThursdayClose(hoursDTO.getClose());
            hours.setFridayClose(hoursDTO.getClose());
            hours.setSaturdayClose(hoursDTO.getClose());
            hours.setSundayClose(hoursDTO.getClose());
        }
    }

    // ==================== Helper Methods ====================

    private String buildFullAddress(BranchCreationDTO dto) {
        StringBuilder address = new StringBuilder();
        
        if (dto.getAddress() != null) {
            address.append(dto.getAddress());
        }
        
        if (dto.getCity() != null) {
            if (address.length() > 0) address.append(", ");
            address.append(dto.getCity());
        }
        
        if (dto.getState() != null) {
            if (address.length() > 0) address.append(", ");
            address.append(dto.getState());
        }
        
        return address.toString();
    }
}

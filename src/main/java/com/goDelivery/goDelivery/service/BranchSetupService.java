package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dto.branch.BranchManagerSetupDTO;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.dtos.restaurant.OperatingHoursDTO;
import com.goDelivery.goDelivery.Enum.ApprovalStatus;
import com.goDelivery.goDelivery.Enum.BranchSetupStatus;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.UnauthorizedException;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.repository.*;

import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchSetupService {

    private final BranchesRepository branchesRepository;
    private final RestaurantMapper restaurantMapper;
    private final BranchUsersRepository branchUsersRepository;
    private final OperatingHoursRepository operatingHoursRepository;
    private final FileStorageService fileStorageService;
    
    private void verifyBranchAccess(Long branchId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Find the branch user
        BranchUsers branchUser = branchUsersRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Branch user not found"));
        
        // Verify the user belongs to this branch
        if (!branchUser.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("You don't have permission to manage this branch");
        }
    }
    
    //Comprehensive branch setup - single API for branch manager to complete all remaining setup.
    @Transactional
    public BranchesDTO completeBranchManagerSetup(Long branchId, BranchManagerSetupDTO setupDTO) {
        // Verify branch access
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        BranchUsers branchUser = branchUsersRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Branch user not found"));
        
        if (!branchUser.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("You don't have permission to setup this branch");
        }
        
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        // Handle file uploads
    try {
        if (setupDTO.getCommercialRegistrationFile() != null && !setupDTO.getCommercialRegistrationFile().isEmpty()) {
            String filePath = fileStorageService.storeFile(
                setupDTO.getCommercialRegistrationFile(), 
                "branches/" + branchId + "/documents/commercialRegistration"
            );
            branch.setCommercialRegistrationCertificateUrl("/api/files/" + filePath.replace("\\", "/"));
        }
        
        if (setupDTO.getTaxIdentificationFile() != null && !setupDTO.getTaxIdentificationFile().isEmpty()) {
            String filePath = fileStorageService.storeFile(
                setupDTO.getTaxIdentificationFile(), 
                "branches/" + branchId + "/documents/taxDocument"
            );
            branch.setTaxIdentificationDocumentUrl("/api/files/" + filePath.replace("\\", "/"));
        }
        
    } catch (IOException e) {
        throw new RuntimeException("Failed to store files: " + e.getMessage());
    }                   
        
        // Update only non-null fields (what branch manager provides)
        updateBranchFromManagerSetup(branch, setupDTO);
        
        // Handle operating hours
        updateOrCreateOperatingHours(branch, setupDTO);
        
        // Mark setup as complete and auto-activate
        branch.setSetupStatus(BranchSetupStatus.COMPLETED);
        branch.setApprovalStatus(ApprovalStatus.APPROVED);  // Auto-approve since parent restaurant is already approved
        branch.setIsActive(true);  // Auto-activate the branch
        branch.setApprovedAt(LocalDate.now());
        
        branchUser.setSetupComplete(true);
        branchUser.setUpdatedAt(LocalDate.now());
        branchUsersRepository.save(branchUser);
        
        Branches savedBranch = branchesRepository.save(branch);
        
        log.info("Branch setup completed and auto-activated for branch {} by user {}", branchId, username);
        
        return restaurantMapper.toBranchDTO(savedBranch);
    }
    

    //Get what fields still need to be set up by the branch manager.
    @Transactional(readOnly = true)
    public BranchManagerSetupDTO getBranchManagerSetupStatus(Long branchId) {
        verifyBranchAccess(branchId);
        
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        BranchManagerSetupDTO dto = new BranchManagerSetupDTO();
        
        // Basic info (mirrors RestaurantDTO)
        dto.setBranchName(branch.getBranchName());
        dto.setAddress(branch.getAddress());
        dto.setCuisineType(branch.getCuisineType());
        dto.setEmail(branch.getEmail());
        dto.setPhoneNumber(branch.getPhoneNumber());
        dto.setLogoUrl(branch.getLogoUrl());
        dto.setDescription(branch.getDescription());
        
        // Delivery settings
        dto.setDeliveryType(branch.getDeliveryType());
        dto.setDeliveryFee(branch.getDeliveryFee());
        dto.setDeliveryRadius(branch.getDeliveryRadius());
        dto.setAveragePreparationTime(branch.getAveragePreparationTime());
        dto.setMinimumOrderAmount(branch.getMinimumOrderAmount());
        
        // Documents
        dto.setCommercialRegistrationCertificateUrl(branch.getCommercialRegistrationCertificateUrl());
        dto.setTaxIdentificationDocumentUrl(branch.getTaxIdentificationDocumentUrl());
        dto.setTaxIdentificationNumber(branch.getTaxIdentificationNumber());
        
        // Operating hours (as nested DTO like RestaurantDTO)
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
    
    private void updateBranchFromManagerSetup(Branches branch, BranchManagerSetupDTO dto) {
        // Basic info - only update if provided (mirrors RestaurantDTO structure)
        if (dto.getBranchName() != null) branch.setBranchName(dto.getBranchName());
        if (dto.getAddress() != null) branch.setAddress(dto.getAddress());
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
    
    private void updateOrCreateOperatingHours(Branches branch, BranchManagerSetupDTO dto) {
        // Check if operating hours are provided
        if (dto.getOperatingHours() == null) {
            return; // No operating hours provided, skip
        }
        
        OperatingHoursDTO hoursDTO = dto.getOperatingHours();
        
        OperatingHours hours = branch.getOperatingHours();
        if (hours == null) {
            hours = new OperatingHours();
            hours.setBranch(branch);
        }
        
        // Update hours from nested DTO (same for all days as per simple OperatingHoursDTO)
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
        
        operatingHoursRepository.save(hours);
    }
}

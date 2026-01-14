package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dto.branch.BranchManagerSetupDTO;
import com.goDelivery.goDelivery.dto.branch.BranchSetupDTO;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.Enum.ApprovalStatus;
import com.goDelivery.goDelivery.Enum.BranchSetupStatus;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.UnauthorizedException;
import com.goDelivery.goDelivery.exception.ValidationException;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchSetupService {

    private final BranchesRepository branchesRepository;
    private final RestaurantMapper restaurantMapper;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final BranchUsersRepository branchUsersRepository;
    private final OperatingHoursRepository operatingHoursRepository;
    
    @Transactional
    public BranchesDTO completeBranchSetup(Long branchId, BranchSetupDTO setupDTO) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Find the branch user
        BranchUsers branchUser = branchUsersRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Branch user not found"));
        
        // Verify the user belongs to this branch
        if (!branchUser.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("You don't have permission to setup this branch");
        }
        
        // Get the branch
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        // Check if setup is already complete
        if (branchUser.isSetupComplete()) {
            throw new ValidationException("Branch setup is already complete");
        }
        
        // Update branch details
        updateBranchDetails(branch, setupDTO);
        
        // Create initial menu categories if provided
        if (setupDTO.getInitialMenuCategories() != null && !setupDTO.getInitialMenuCategories().isEmpty()) {
            createInitialMenuCategories(branch, setupDTO.getInitialMenuCategories());
        }
        
        // Mark setup as complete and auto-activate
        branchUser.setSetupComplete(true);
        branchUser.setUpdatedAt(LocalDate.now());
        branchUsersRepository.save(branchUser);
        
        // Auto-approve and activate since parent restaurant is already approved
        branch.setApprovalStatus(ApprovalStatus.APPROVED);
        branch.setIsActive(true);
        branch.setApprovedAt(LocalDate.now());
        
        Branches savedBranch = branchesRepository.save(branch);
        
        log.info("Branch setup completed and auto-activated for branch {} by user {}", branchId, username);
        
        return restaurantMapper.toBranchDTO(savedBranch);
    }
    
    @Transactional(readOnly = true)
    public BranchSetupDTO getBranchSetupStatus(Long branchId) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Find the branch user
        BranchUsers branchUser = branchUsersRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Branch user not found"));
        
        // Verify the user belongs to this branch
        if (!branchUser.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("You don't have permission to view this branch setup");
        }
        
        // Get the branch
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        // Convert to setup DTO
        BranchSetupDTO setupDTO = new BranchSetupDTO();
        setupDTO.setBranchName(branch.getBranchName());
        setupDTO.setAddress(branch.getAddress());
        setupDTO.setLatitude(branch.getLatitude());
        setupDTO.setLongitude(branch.getLongitude());
        setupDTO.setPhoneNumber(branch.getPhoneNumber());
        setupDTO.setOperatingHours(branch.getOperatingHours() != null ? branch.getOperatingHours().toString() : null);
        setupDTO.setDescription(branch.getDescription());
        
        return setupDTO;
    }
    
    @Transactional
    public BranchesDTO updateBranchLocation(Long branchId, Float latitude, Float longitude, 
                                           String address) {
        // Verify branch access
        verifyBranchAccess(branchId);
        
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        // Update location
        branch.setLatitude(latitude);
        branch.setLongitude(longitude);
        branch.setAddress(address);
        branch.setUpdatedAt(LocalDate.now());
        
        Branches savedBranch = branchesRepository.save(branch);
        log.info("Updated location for branch {}: {}, {}", branchId, latitude, longitude);
        
        return restaurantMapper.toBranchDTO(savedBranch);
    }
    
    private void updateBranchDetails(Branches branch, BranchSetupDTO setupDTO) {
        branch.setBranchName(setupDTO.getBranchName());
        branch.setAddress(setupDTO.getAddress());
        branch.setLatitude(setupDTO.getLatitude());
        branch.setLongitude(setupDTO.getLongitude());
        branch.setPhoneNumber(setupDTO.getPhoneNumber());
        branch.setDescription(setupDTO.getDescription());
        branch.setUpdatedAt(LocalDate.now());
    }
    
    private void createInitialMenuCategories(Branches branch, List<String> categoryNames) {
        for (int i = 0; i < categoryNames.size(); i++) {
            String categoryName = categoryNames.get(i);
            
            // Create menu category
            MenuCategory category = new MenuCategory();
            category.setCategoryName(categoryName);
            category.setBranch(branch);
            
            menuCategoryRepository.save(category);
            log.info("Created initial menu category '{}' for branch {}", categoryName, branch.getBranchId());
        }
    }
    
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
    
    /**
     * Comprehensive branch setup - single API for branch manager to complete all remaining setup.
     * Only updates fields that are provided (not null) in the DTO.
     * This allows branch manager to fill in only what restaurant admin didn't set.
     */
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
        
        // Update only non-null fields (what branch manager provides)
        updateBranchFromManagerSetup(branch, setupDTO);
        
        // Handle operating hours
        updateOrCreateOperatingHours(branch, setupDTO);
        
        // Mark setup as complete and auto-activate
        branch.setSetupStatus(BranchSetupStatus.SETUP_COMPLETE);
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
    
    /**
     * Get what fields still need to be set up by the branch manager.
     * Returns the current branch state so frontend can show which fields are missing.
     */
    @Transactional(readOnly = true)
    public BranchManagerSetupDTO getBranchManagerSetupStatus(Long branchId) {
        verifyBranchAccess(branchId);
        
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        BranchManagerSetupDTO dto = new BranchManagerSetupDTO();
        
        // Basic info
        dto.setBranchName(branch.getBranchName());
        dto.setAddress(branch.getAddress());
        dto.setPhoneNumber(branch.getPhoneNumber());
        dto.setEmail(branch.getEmail());
        dto.setDescription(branch.getDescription());
        
        // Location
        dto.setLatitude(branch.getLatitude());
        dto.setLongitude(branch.getLongitude());
        
        // Branding
        dto.setLogoUrl(branch.getLogoUrl());
        
        // Delivery settings
        dto.setDeliveryType(branch.getDeliveryType());
        dto.setDeliveryFee(branch.getDeliveryFee());
        dto.setMinimumOrderAmount(branch.getMinimumOrderAmount());
        dto.setDeliveryRadius(branch.getDeliveryRadius());
        dto.setAveragePreparationTime(branch.getAveragePreparationTime());
        dto.setDeliveryAvailable(branch.getDeliveryAvailable());
        
        // Documents
        dto.setBusinessDocumentUrl(branch.getBusinessDocumentUrl());
        dto.setOperatingLicenseUrl(branch.getOperatingLicenseUrl());
        
        // Operating hours
        OperatingHours hours = branch.getOperatingHours();
        if (hours != null) {
            dto.setMondayOpen(parseTime(hours.getMondayOpen()));
            dto.setMondayClose(parseTime(hours.getMondayClose()));
            dto.setTuesdayOpen(parseTime(hours.getTuesdayOpen()));
            dto.setTuesdayClose(parseTime(hours.getTuesdayClose()));
            dto.setWednesdayOpen(parseTime(hours.getWednesdayOpen()));
            dto.setWednesdayClose(parseTime(hours.getWednesdayClose()));
            dto.setThursdayOpen(parseTime(hours.getThursdayOpen()));
            dto.setThursdayClose(parseTime(hours.getThursdayClose()));
            dto.setFridayOpen(parseTime(hours.getFridayOpen()));
            dto.setFridayClose(parseTime(hours.getFridayClose()));
            dto.setSaturdayOpen(parseTime(hours.getSaturdayOpen()));
            dto.setSaturdayClose(parseTime(hours.getSaturdayClose()));
            dto.setSundayOpen(parseTime(hours.getSundayOpen()));
            dto.setSundayClose(parseTime(hours.getSundayClose()));
        }
        
        return dto;
    }
    
    private void updateBranchFromManagerSetup(Branches branch, BranchManagerSetupDTO dto) {
        // Basic info - only update if provided
        if (dto.getBranchName() != null) branch.setBranchName(dto.getBranchName());
        if (dto.getAddress() != null) branch.setAddress(dto.getAddress());
        if (dto.getPhoneNumber() != null) branch.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getEmail() != null) branch.setEmail(dto.getEmail());
        if (dto.getDescription() != null) branch.setDescription(dto.getDescription());
        
        // Location
        if (dto.getLatitude() != null) branch.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) branch.setLongitude(dto.getLongitude());
        
        // Branding
        if (dto.getLogoUrl() != null) branch.setLogoUrl(dto.getLogoUrl());
        
        // Delivery settings
        if (dto.getDeliveryType() != null) branch.setDeliveryType(dto.getDeliveryType());
        if (dto.getDeliveryFee() != null) branch.setDeliveryFee(dto.getDeliveryFee());
        if (dto.getMinimumOrderAmount() != null) branch.setMinimumOrderAmount(dto.getMinimumOrderAmount());
        if (dto.getDeliveryRadius() != null) branch.setDeliveryRadius(dto.getDeliveryRadius());
        if (dto.getAveragePreparationTime() != null) branch.setAveragePreparationTime(dto.getAveragePreparationTime());
        if (dto.getDeliveryAvailable() != null) branch.setDeliveryAvailable(dto.getDeliveryAvailable());
        
        // Documents
        if (dto.getBusinessDocumentUrl() != null) branch.setBusinessDocumentUrl(dto.getBusinessDocumentUrl());
        if (dto.getOperatingLicenseUrl() != null) branch.setOperatingLicenseUrl(dto.getOperatingLicenseUrl());
        
        branch.setUpdatedAt(LocalDate.now());
    }
    
    private void updateOrCreateOperatingHours(Branches branch, BranchManagerSetupDTO dto) {
        // Check if any operating hours are provided
        boolean hasOperatingHours = dto.getMondayOpen() != null || dto.getTuesdayOpen() != null ||
                dto.getWednesdayOpen() != null || dto.getThursdayOpen() != null ||
                dto.getFridayOpen() != null || dto.getSaturdayOpen() != null ||
                dto.getSundayOpen() != null;
        
        if (!hasOperatingHours) {
            return; // No operating hours provided, skip
        }
        
        OperatingHours hours = branch.getOperatingHours();
        if (hours == null) {
            hours = new OperatingHours();
            hours.setBranch(branch);
        }
        
        // Update only provided hours
        if (dto.getMondayOpen() != null) hours.setMondayOpen(dto.getMondayOpen().toString());
        if (dto.getMondayClose() != null) hours.setMondayClose(dto.getMondayClose().toString());
        if (dto.getTuesdayOpen() != null) hours.setTuesdayOpen(dto.getTuesdayOpen().toString());
        if (dto.getTuesdayClose() != null) hours.setTuesdayClose(dto.getTuesdayClose().toString());
        if (dto.getWednesdayOpen() != null) hours.setWednesdayOpen(dto.getWednesdayOpen().toString());
        if (dto.getWednesdayClose() != null) hours.setWednesdayClose(dto.getWednesdayClose().toString());
        if (dto.getThursdayOpen() != null) hours.setThursdayOpen(dto.getThursdayOpen().toString());
        if (dto.getThursdayClose() != null) hours.setThursdayClose(dto.getThursdayClose().toString());
        if (dto.getFridayOpen() != null) hours.setFridayOpen(dto.getFridayOpen().toString());
        if (dto.getFridayClose() != null) hours.setFridayClose(dto.getFridayClose().toString());
        if (dto.getSaturdayOpen() != null) hours.setSaturdayOpen(dto.getSaturdayOpen().toString());
        if (dto.getSaturdayClose() != null) hours.setSaturdayClose(dto.getSaturdayClose().toString());
        if (dto.getSundayOpen() != null) hours.setSundayOpen(dto.getSundayOpen().toString());
        if (dto.getSundayClose() != null) hours.setSundayClose(dto.getSundayClose().toString());
        
        operatingHoursRepository.save(hours);
    }
    
    private java.time.LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalTime.parse(timeStr);
        } catch (Exception e) {
            return null;
        }
    }
}

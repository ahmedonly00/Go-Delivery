package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.BranchSetupStatus;
import com.goDelivery.goDelivery.Enum.DeliveryType;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.ValidationException;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.model.BranchUsers;
import com.goDelivery.goDelivery.model.Branches;
import com.goDelivery.goDelivery.model.OperatingHours;
import com.goDelivery.goDelivery.repository.BranchUsersRepository;
import com.goDelivery.goDelivery.repository.BranchesRepository;
import com.goDelivery.goDelivery.repository.MenuCategoryRepository;
import com.goDelivery.goDelivery.repository.OperatingHoursRepository;
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
public class BranchSetupProgressService {
    
    private final BranchUsersRepository branchUsersRepository;
    private final BranchesRepository branchesRepository;
    private final RestaurantMapper restaurantMapper;
    private final OperatingHoursRepository operatingHoursRepository;
    private final BranchMenuInheritanceService branchMenuInheritanceService;
    private final MenuCategoryRepository menuCategoryRepository;
    
    @Transactional(readOnly = true)
    public BranchesDTO getBranchSetupStatus(Long branchId) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Find the branch user
        BranchUsers branchUser = branchUsersRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Branch user not found"));
        
        // Verify the user belongs to this branch
        if (!branchUser.getBranch().getBranchId().equals(branchId)) {
            throw new ValidationException("You don't have permission to view this branch setup");
        }
        
        // Get the branch
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        return restaurantMapper.toBranchDTO(branch);
    }
    
    @Transactional
    public BranchesDTO updateBranchSetupStatus(Long branchId, BranchSetupStatus newStatus) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Find the branch user
        BranchUsers branchUser = branchUsersRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Branch user not found"));
        
        // Verify the user belongs to this branch
        if (!branchUser.getBranch().getBranchId().equals(branchId)) {
            throw new ValidationException("You don't have permission to update this branch");
        }
        
        // Get the branch
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        // Update setup status
        branch.setSetupStatus(newStatus);
        branch.setUpdatedAt(LocalDate.now());
        
        // If setup is completed, mark user setup as complete
        if (newStatus == BranchSetupStatus.COMPLETED) {
            branchUser.setSetupComplete(true);
            branchUser.setUpdatedAt(LocalDate.now());
            branchUsersRepository.save(branchUser);
            
            // Activate the branch if it was approved
            if (branch.getApprovalStatus() == com.goDelivery.goDelivery.Enum.ApprovalStatus.APPROVED) {
                branch.setIsActive(true);
                branch.setSetupStatus(BranchSetupStatus.ACTIVE);
            }
        }
        
        Branches savedBranch = branchesRepository.save(branch);
        
        log.info("Branch setup status updated to {} for branch {} by user {}", 
                newStatus, branchId, username);
        
        return restaurantMapper.toBranchDTO(savedBranch);
    }
    
    @Transactional
    public BranchesDTO addLocationDetails(Long branchId, String address, Float latitude, Float longitude) {
        Branches branch = getBranchForCurrentUser(branchId);
        
        branch.setAddress(address);
        branch.setLatitude(latitude);
        branch.setLongitude(longitude);
        branch.setSetupStatus(BranchSetupStatus.LOCATION_ADDED);
        branch.setUpdatedAt(LocalDate.now());
        
        Branches savedBranch = branchesRepository.save(branch);
        
        log.info("Location details added for branch {}", branchId);
        
        return restaurantMapper.toBranchDTO(savedBranch);
    }
    
    @Transactional
    public BranchesDTO configureDeliverySettings(Long branchId, DeliveryType deliveryType, 
                                                 Float deliveryFee, Double deliveryRadius, 
                                                 Float minimumOrderAmount, Integer averagePrepTime) {
        Branches branch = getBranchForCurrentUser(branchId);
        
        branch.setDeliveryType(deliveryType);
        branch.setDeliveryFee(deliveryFee);
        branch.setDeliveryRadius(deliveryRadius);
        branch.setMinimumOrderAmount(minimumOrderAmount);
        branch.setAveragePreparationTime(averagePrepTime);
        branch.setDeliveryAvailable(deliveryType != null);
        branch.setSetupStatus(BranchSetupStatus.SETTINGS_CONFIGURED);
        branch.setUpdatedAt(LocalDate.now());
        
        Branches savedBranch = branchesRepository.save(branch);
        
        log.info("Delivery settings configured for branch {}", branchId);
        
        return restaurantMapper.toBranchDTO(savedBranch);
    }
    
    @Transactional
    public BranchesDTO addOperatingHours(Long branchId, OperatingHours operatingHours) {
        Branches branch = getBranchForCurrentUser(branchId);
        
        // Save operating hours
        operatingHours.setBranch(branch);
        operatingHoursRepository.save(operatingHours);
        
        // Update branch
        branch.setSetupStatus(BranchSetupStatus.OPERATING_HOURS_ADDED);
        branch.setUpdatedAt(LocalDate.now());
        
        Branches savedBranch = branchesRepository.save(branch);
        
        log.info("Operating hours added for branch {}", branchId);
        
        return restaurantMapper.toBranchDTO(savedBranch);
    }
    
    @Transactional
    public BranchesDTO addBranding(Long branchId, String logoUrl, String description) {
        Branches branch = getBranchForCurrentUser(branchId);
        
        branch.setLogoUrl(logoUrl);
        branch.setDescription(description);
        branch.setSetupStatus(BranchSetupStatus.BRANDING_ADDED);
        branch.setUpdatedAt(LocalDate.now());
        
        Branches savedBranch = branchesRepository.save(branch);
        
        log.info("Branding added for branch {}", branchId);
        
        return restaurantMapper.toBranchDTO(savedBranch);
    }
    
    @Transactional
    public BranchesDTO startMenuSetup(Long branchId) {
        Branches branch = getBranchForCurrentUser(branchId);
        
        // Inherit menu from restaurant if branch doesn't have menu yet
        long existingMenuCount = menuCategoryRepository.countByBranch_BranchId(branchId);
        if (existingMenuCount == 0) {
            try {
                log.info("Inheriting menu from restaurant for branch {}", branchId);
                branchMenuInheritanceService.inheritRestaurantMenu(branchId);
            } catch (ValidationException e) {
                log.warn("Menu inheritance failed for branch {}: {}", branchId, e.getMessage());
                // Continue even if inheritance fails
            }
        }
        
        branch.setSetupStatus(BranchSetupStatus.MENU_SETUP_STARTED);
        branch.setUpdatedAt(LocalDate.now());
        
        Branches savedBranch = branchesRepository.save(branch);
        
        log.info("Menu setup started for branch {}", branchId);
        
        return restaurantMapper.toBranchDTO(savedBranch);
    }
    
    @Transactional
    public BranchesDTO completeMenuSetup(Long branchId) {
        Branches branch = getBranchForCurrentUser(branchId);
        
        branch.setSetupStatus(BranchSetupStatus.MENU_SETUP_COMPLETED);
        branch.setUpdatedAt(LocalDate.now());
        
        Branches savedBranch = branchesRepository.save(branch);
        
        log.info("Menu setup completed for branch {}", branchId);
        
        return restaurantMapper.toBranchDTO(savedBranch);
    }
    
    @Transactional
    public BranchesDTO completeBranchSetup(Long branchId) {
        return updateBranchSetupStatus(branchId, BranchSetupStatus.COMPLETED);
    }
    
    private Branches getBranchForCurrentUser(Long branchId) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Find the branch user
        BranchUsers branchUser = branchUsersRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Branch user not found"));
        
        // Verify the user belongs to this branch
        if (!branchUser.getBranch().getBranchId().equals(branchId)) {
            throw new ValidationException("You don't have permission to update this branch");
        }
        
        // Get the branch
        return branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
    }
}

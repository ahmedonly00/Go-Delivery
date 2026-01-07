package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dto.branch.BranchSetupDTO;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.Enum.ApprovalStatus;
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
        
        // Mark setup as complete
        branchUser.setSetupComplete(true);
        branchUser.setUpdatedAt(LocalDate.now());
        branchUsersRepository.save(branchUser);
        
        // Activate the branch if it was approved
        if (branch.getApprovalStatus() == ApprovalStatus.APPROVED) {
            branch.setActive(true);
        }
        
        Branches savedBranch = branchesRepository.save(branch);
        
        log.info("Branch setup completed for branch {} by user {}", branchId, username);
        
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
        setupDTO.setOperatingHours(branch.getOperatingHours());
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
        branch.setOperatingHours(setupDTO.getOperatingHours());
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
}

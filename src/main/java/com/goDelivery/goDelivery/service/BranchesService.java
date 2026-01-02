package com.goDelivery.goDelivery.service;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.platform.commons.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.UnauthorizedException;
import com.goDelivery.goDelivery.exception.ValidationException;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.model.Branches;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.BranchesRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import com.goDelivery.goDelivery.repository.UsersRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BranchesService {

    private final BranchesRepository branchesRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final UsersRepository usersRepository;
    private final UsersService usersService;

    public BranchesService(BranchesRepository branchesRepository, RestaurantRepository restaurantRepository, RestaurantMapper restaurantMapper, UsersRepository usersRepository, UsersService usersService) {
        this.branchesRepository = branchesRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantMapper = restaurantMapper;
        this.usersRepository = usersRepository;
        this.usersService = usersService;
    }
    
    @Transactional
    public BranchesDTO addBranchToRestaurant(Long restaurantId, BranchesDTO branchDTO) {
        // Get current user and verify they are a restaurant admin for this restaurant
        RestaurantUsers currentUser = usersService.getCurrentUser();
        
        // Verify the user belongs to the specified restaurant
        if (currentUser.getRestaurant() == null || 
            !currentUser.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new UnauthorizedException("You don't have permission to add branches to this restaurant");
        }
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        
        // Validate branch data
        validateBranchData(branchDTO);
        
        // Check if branch name already exists for this restaurant
        if (branchesRepository.existsByRestaurant_RestaurantIdAndBranchName(restaurantId, branchDTO.getBranchName())) {
            throw new ValidationException("A branch with this name already exists for this restaurant");
        }
        
        Branches branch = restaurantMapper.toBranch(branchDTO);
        branch.setRestaurant(restaurant);
        branch.setActive(true); // New branches are active by default
        
        Branches savedBranch = branchesRepository.save(branch);
        log.info("Created new branch '{}' for restaurant '{}' by user '{}'", 
                branch.getBranchName(), restaurant.getRestaurantName(), currentUser.getEmail());

        return restaurantMapper.toBranchDTO(savedBranch);
    }

    public BranchesDTO getBranchById(Long branchId) {
        Branches branches = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        return restaurantMapper.toBranchDTO(branches);
    }

    @Transactional(readOnly = true)
    public List<BranchesDTO> getBranchesByRestaurant(Long restaurantId, String userEmail) {
        // Verify user has permission to view this restaurant's branches
        verifyRestaurantAccess(restaurantId, userEmail);
        
        return branchesRepository.findByRestaurant_RestaurantId(restaurantId).stream()
                .map(restaurantMapper::toBranchDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BranchesDTO updateBranch(Long branchId, BranchesDTO branchDTO, String userEmail) {
        Branches existingBranch = getBranchWithPermissionCheck(branchId, userEmail);
        
        // Validate branch data
        validateBranchData(branchDTO);
        
        // Check if new branch name conflicts with existing branches (excluding this one)
        if (!existingBranch.getBranchName().equals(branchDTO.getBranchName()) &&
            branchesRepository.existsByRestaurant_RestaurantIdAndBranchName(
                existingBranch.getRestaurant().getRestaurantId(), branchDTO.getBranchName())) {
            throw new ValidationException("A branch with this name already exists for this restaurant");
        }

        // Update fields from DTO
        existingBranch.setBranchName(branchDTO.getBranchName());
        existingBranch.setAddress(branchDTO.getAddress());
        existingBranch.setLatitude(branchDTO.getLatitude());
        existingBranch.setLongitude(branchDTO.getLongitude());
        existingBranch.setPhoneNumber(branchDTO.getPhoneNumber());
        existingBranch.setOperatingHours(branchDTO.getOperatingHours());
        existingBranch.setActive(branchDTO.isActive());
        existingBranch.setUpdatedAt(java.time.LocalDate.now());

        Branches updatedBranch = branchesRepository.save(existingBranch);
        log.info("Updated branch '{}' by user '{}'", branchId, userEmail);

        return restaurantMapper.toBranchDTO(updatedBranch);
    }

    @Transactional
    public void deleteBranch(Long branchId, String userEmail) {
        Branches branch = getBranchWithPermissionCheck(branchId, userEmail);
        
        // Check if branch has orders
        if (!branch.getOrders().isEmpty()) {
            throw new ValidationException("Cannot delete branch with existing orders. Deactivate it instead.");
        }
        
        branchesRepository.delete(branch);
        log.info("Deleted branch '{}' by user '{}'", branchId, userEmail);
    }

    /**
     * Get all branches for the current user's restaurant
     */
    @Transactional(readOnly = true)
    public List<BranchesDTO> getCurrentUserRestaurantBranches() {
        RestaurantUsers currentUser = usersService.getCurrentUser();
        
        if (currentUser.getRestaurant() == null) {
            throw new UnauthorizedException("User is not associated with any restaurant");
        }
        
        return branchesRepository.findByRestaurant_RestaurantId(currentUser.getRestaurant().getRestaurantId())
                .stream()
                .map(restaurantMapper::toBranchDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a specific branch if it belongs to the current user's restaurant
     */
    @Transactional(readOnly = true)
    public BranchesDTO getBranchForCurrentUser(Long branchId) {
        RestaurantUsers currentUser = usersService.getCurrentUser();
        
        if (currentUser.getRestaurant() == null) {
            throw new UnauthorizedException("User is not associated with any restaurant");
        }
        
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        
        // Verify branch belongs to user's restaurant
        if (!branch.getRestaurant().getRestaurantId().equals(currentUser.getRestaurant().getRestaurantId())) {
            throw new UnauthorizedException("You don't have permission to view this branch");
        }
        
        return restaurantMapper.toBranchDTO(branch);
    }
    
    /**
     * Check if a branch belongs to the specified restaurant
     */
    @Transactional(readOnly = true)
    public boolean isBranchBelongsToRestaurant(Long branchId, Long restaurantId) {
        return branchesRepository.existsByBranchIdAndRestaurant_RestaurantId(branchId, restaurantId);
    }
    
    /**
     * Get restaurant ID for a branch
     */
    @Transactional(readOnly = true)
    public Long getRestaurantIdByBranch(Long branchId) {
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        return branch.getRestaurant().getRestaurantId();
    }
    
    public List<BranchesDTO> getAllBranches() {
        List<Branches> branches = branchesRepository.findAll();
        return branches.stream()
                .map(restaurantMapper::toBranchDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BranchesDTO toggleBranchStatus(Long branchId, boolean isActive, String userEmail) {
        Branches branch = getBranchWithPermissionCheck(branchId, userEmail);
        branch.setActive(isActive);
        Branches updatedBranch = branchesRepository.save(branch);
        log.info("{} branch {}", isActive ? "Activated" : "Deactivated", branchId);
        return restaurantMapper.toBranchDTO(updatedBranch);
    }
    // Helper methods
    private Restaurant verifyRestaurantAccess(Long restaurantId, String userEmail) {
        // Check if user has access to this restaurant
        if (!usersRepository.existsByRestaurant_RestaurantIdAndEmail(restaurantId, userEmail)) {
            throw new UnauthorizedException("You don't have permission to manage this restaurant");
        }
        
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
    }

    private Branches getBranchWithPermissionCheck(Long branchId, String userEmail) {
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        
        // Verify user has access to the restaurant this branch belongs to
        verifyRestaurantAccess(branch.getRestaurant().getRestaurantId(), userEmail);
        
        return branch;
    }

    private void validateBranchData(BranchesDTO branchDTO) {
        if (StringUtils.isBlank(branchDTO.getBranchName())) {
            throw new ValidationException("Branch name is required");
        }
        if (StringUtils.isBlank(branchDTO.getAddress())) {
            throw new ValidationException("Branch address is required");
        }
        if (StringUtils.isBlank(branchDTO.getPhoneNumber())) {
            throw new ValidationException("Phone number is required");
        }
        if (branchDTO.getLatitude() == null || branchDTO.getLongitude() == null) {
            throw new ValidationException("Branch coordinates (latitude and longitude) are required");
        }
        if (StringUtils.isBlank(branchDTO.getOperatingHours())) {
            throw new ValidationException("Operating hours are required");
        }
        // Validate phone number format (basic validation)
        if (!branchDTO.getPhoneNumber().matches("^[+]?[0-9]{10,15}$")) {
            throw new ValidationException("Invalid phone number format");
        }
    }
}

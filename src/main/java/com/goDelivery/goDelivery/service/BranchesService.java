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

    public BranchesService(BranchesRepository branchesRepository, RestaurantRepository restaurantRepository, RestaurantMapper restaurantMapper, UsersRepository usersRepository) {
        this.branchesRepository = branchesRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantMapper = restaurantMapper;
        this.usersRepository = usersRepository;
    }
    
    public BranchesDTO addBranchToRestaurant(Long restaurantId, BranchesDTO branchDTO) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        
        Branches branch = restaurantMapper.toBranch(branchDTO);
        branch.setRestaurant(restaurant);
        
        Branches savedBranch = branchesRepository.save(branch);
        log.info("Created new branch {} for restaurant {}", branch.getBranchName(), restaurant.getRestaurantName());

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

    public BranchesDTO updateBranch(Long branchId, BranchesDTO branchDTO) {
        Branches existingBranch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));

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
        log.info("Updated branch {}", branchId);

        return restaurantMapper.toBranchDTO(updatedBranch);
    }

    public void deleteBranch(Long branchId) {
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        branchesRepository.delete(branch);
        log.info("Deleted branch {}", branchId);
    }

    public List<BranchesDTO> getAllBranches() {
        List<Branches> branches = branchesRepository.findAll();
        return branches.stream()
                .map(restaurantMapper::toBranchDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleBranchStatus(Long branchId, boolean isActive, String userEmail) {
        Branches branch = getBranchWithPermissionCheck(branchId, userEmail);
        branch.setActive(isActive);
        branchesRepository.save(branch);
        log.info("{} branch {}", isActive ? "Activated" : "Deactivated", branchId);
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
        // Add more validations as needed
    }
}

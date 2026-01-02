package com.goDelivery.goDelivery.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.dtos.restaurant.BranchUserDTO;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.UnauthorizedException;
import com.goDelivery.goDelivery.exception.ValidationException;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.model.BranchUsers;
import com.goDelivery.goDelivery.model.Branches;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.BranchUsersRepository;
import com.goDelivery.goDelivery.repository.BranchesRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchUserService {

    private final BranchUsersRepository branchUsersRepository;
    private final BranchesRepository branchesRepository;
    private final RestaurantMapper restaurantMapper;
    private final PasswordEncoder passwordEncoder;
    private final UsersService usersService;

    @Transactional
    public BranchUserDTO createBranchUser(Long branchId, BranchUserDTO branchUserDTO) {
        // Get current user and verify they are a restaurant admin
        RestaurantUsers currentUser = usersService.getCurrentUser();
        
        // Get the branch
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        
        // Verify the branch belongs to the current user's restaurant
        if (!branch.getRestaurant().getRestaurantId().equals(currentUser.getRestaurant().getRestaurantId())) {
            throw new UnauthorizedException("You don't have permission to add users to this branch");
        }
        
        // Validate email uniqueness
        if (branchUsersRepository.existsByEmail(branchUserDTO.getEmail())) {
            throw new ValidationException("Email already exists");
        }
        
        // Validate branch email uniqueness
        if (branchUsersRepository.existsByBranch_BranchIdAndEmail(branchId, branchUserDTO.getEmail())) {
            throw new ValidationException("Email already exists for this branch");
        }
        
        // Create branch user
        BranchUsers branchUser = restaurantMapper.toBranchUser(branchUserDTO);
        branchUser.setBranch(branch);
        branchUser.setRestaurant(branch.getRestaurant());
        branchUser.setPassword(passwordEncoder.encode(branchUserDTO.getPassword()));
        branchUser.setActive(true);
        branchUser.setEmailVerified(false);
        
        // Set role based on input or default to BRANCH_MANAGER
        if (branchUserDTO.getRole() != null) {
            branchUser.setRole(branchUserDTO.getRole());
        } else {
            branchUser.setRole(Roles.BRANCH_MANAGER);
        }
        
        BranchUsers savedUser = branchUsersRepository.save(branchUser);
        log.info("Created branch user '{}' for branch '{}' by restaurant admin '{}'", 
                savedUser.getEmail(), branch.getBranchName(), currentUser.getEmail());
        
        return restaurantMapper.toBranchUserDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public List<BranchUserDTO> getBranchUsers(Long branchId) {
        // Get current user and verify they have permission
        RestaurantUsers currentUser = usersService.getCurrentUser();
        
        // Get the branch
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        
        // Verify the branch belongs to the current user's restaurant
        if (!branch.getRestaurant().getRestaurantId().equals(currentUser.getRestaurant().getRestaurantId())) {
            throw new UnauthorizedException("You don't have permission to view users of this branch");
        }
        
        return branchUsersRepository.findByBranch_BranchId(branchId).stream()
                .map(restaurantMapper::toBranchUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BranchUserDTO updateBranchUser(Long userId, BranchUserDTO branchUserDTO) {
        // Get current user
        RestaurantUsers currentUser = usersService.getCurrentUser();
        
        // Get the branch user to update
        BranchUsers existingUser = branchUsersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch user not found with id: " + userId));
        
        // Verify the branch belongs to the current user's restaurant
        if (!existingUser.getBranch().getRestaurant().getRestaurantId().equals(currentUser.getRestaurant().getRestaurantId())) {
            throw new UnauthorizedException("You don't have permission to update this user");
        }
        
        // Update fields
        if (branchUserDTO.getFullName() != null) {
            existingUser.setFullName(branchUserDTO.getFullName());
        }
        if (branchUserDTO.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(branchUserDTO.getPhoneNumber());
        }
        if (branchUserDTO.getRole() != null) {
            existingUser.setRole(branchUserDTO.getRole());
        }
        if (branchUserDTO.getPermissions() != null) {
            existingUser.setPermissions(branchUserDTO.getPermissions());
        }
        if (branchUserDTO.getPassword() != null && !branchUserDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(branchUserDTO.getPassword()));
        }
        
        BranchUsers updatedUser = branchUsersRepository.save(existingUser);
        log.info("Updated branch user '{}' by restaurant admin '{}'", updatedUser.getEmail(), currentUser.getEmail());
        
        return restaurantMapper.toBranchUserDTO(updatedUser);
    }

    @Transactional
    public void toggleBranchUserStatus(Long userId, boolean isActive) {
        // Get current user
        RestaurantUsers currentUser = usersService.getCurrentUser();
        
        // Get the branch user
        BranchUsers branchUser = branchUsersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch user not found with id: " + userId));
        
        // Verify the branch belongs to the current user's restaurant
        if (!branchUser.getBranch().getRestaurant().getRestaurantId().equals(currentUser.getRestaurant().getRestaurantId())) {
            throw new UnauthorizedException("You don't have permission to modify this user");
        }
        
        branchUser.setActive(isActive);
        branchUsersRepository.save(branchUser);
        
        log.info("{} branch user '{}' by restaurant admin '{}'", 
                isActive ? "Activated" : "Deactivated", branchUser.getEmail(), currentUser.getEmail());
    }

    @Transactional(readOnly = true)
    public List<BranchUserDTO> getActiveBranchUsers(Long branchId) {
        // Get current user and verify permission
        RestaurantUsers currentUser = usersService.getCurrentUser();
        
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        
        if (!branch.getRestaurant().getRestaurantId().equals(currentUser.getRestaurant().getRestaurantId())) {
            throw new UnauthorizedException("You don't have permission to view users of this branch");
        }
        
        return branchUsersRepository.findByBranch_BranchIdAndIsActive(branchId, true).stream()
                .map(restaurantMapper::toBranchUserDTO)
                .collect(Collectors.toList());
    }
}

package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.user.RestaurantUserRequest;
import com.goDelivery.goDelivery.dtos.user.RestaurantUserResponse;
import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.exception.ConflictException;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.UnauthorizedException;
import com.goDelivery.goDelivery.exception.ValidationException;
import com.goDelivery.goDelivery.mapper.RestaurantUserMapper;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import com.goDelivery.goDelivery.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class  UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantUserMapper restaurantUserMapper;
    private final RestaurantRepository restaurantRepository;


    public RestaurantUserResponse createUser(RestaurantUserRequest request, String adminEmail) {
        // Validate admin permissions and restaurant access
        validateAdminRestaurantAccess(adminEmail, request.getRestaurantId());
        
        // Check if email already exists
        if (usersRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User with email " + request.getEmail() + " already exists");
        }
        
        // Validate password is provided for new users
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ValidationException("Password is required for new users");
        }
        
        // Set default role if not provided
        if (request.getRole() == null) {
            request.setRole(Roles.CASHIER);
        }
       
        // Set default permissions if not provided
        if (request.getPermissions() == null || request.getPermissions().isBlank()) {
            request.setPermissions("READ_ORDERS,UPDATE_ORDERS");
        }
       
        // Map to entity
        RestaurantUsers user = restaurantUserMapper.mapToEntity(request);
        
        // Encode password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Set audit fields
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Save user
        RestaurantUsers savedUser = usersRepository.save(user);
        
        return restaurantUserMapper.mapToResponse(savedUser);
    }


    public RestaurantUserResponse updateUser(Long userId, RestaurantUserRequest request, String adminEmail) {
        // Optional: Add authorization check
        validateAdminPermissions(adminEmail);
        
        RestaurantUsers user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    
        // Update fields only if provided (null-safe updates)
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
    
        // Only update password if provided and not empty
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
    
        // Update role if provided
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        
        // Update permissions if provided
        if (request.getPermissions() != null) {
            user.setPermissions(request.getPermissions());
        }
    
        // Use LocalDateTime for better precision
        user.setUpdatedAt(LocalDateTime.now());
        
        RestaurantUsers updatedUser = usersRepository.save(user);
    
        return restaurantUserMapper.mapToResponse(updatedUser);
    }


    public void deleteUser(Long userId, String adminEmail) {
        validateAdminPermissions(adminEmail);
        RestaurantUsers user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        usersRepository.delete(user);
    }


    public RestaurantUserResponse getUserById(Long userId, String adminEmail) {
        validateAdminPermissions(adminEmail);
        RestaurantUsers user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return restaurantUserMapper.mapToResponse(user);
    }


    public List<RestaurantUserResponse> getAllUsersByRestaurant(Long restaurantId, String adminEmail) {
        validateAdminPermissions(adminEmail);
        return usersRepository.findByRestaurant_RestaurantId(restaurantId).stream()
                .map(restaurantUserMapper::mapToResponse)
                .collect(Collectors.toList());
    }
    

    public List<RestaurantUserResponse> getActiveUsersByRestaurant(Long restaurantId, String adminEmail) {
        validateAdminPermissions(adminEmail);
        return usersRepository.findByRestaurant_RestaurantIdAndIsActiveTrue(restaurantId).stream()
                .map(restaurantUserMapper::mapToResponse)
                .collect(Collectors.toList());
    }


    public RestaurantUserResponse updateUserRole(Long userId, Roles role, String adminEmail) {
        validateAdminPermissions(adminEmail);
        RestaurantUsers user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        RestaurantUsers updatedUser = usersRepository.save(user);
        
        return restaurantUserMapper.mapToResponse(updatedUser);
    }


    public RestaurantUserResponse deactivateUser(Long userId, String adminEmail) {
        validateAdminPermissions(adminEmail);
        RestaurantUsers user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        RestaurantUsers updatedUser = usersRepository.save(user);
        
        return restaurantUserMapper.mapToResponse(updatedUser);
    }


    public RestaurantUserResponse activateUser(Long userId, String adminEmail) {
        validateAdminPermissions(adminEmail);
        RestaurantUsers user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        RestaurantUsers updatedUser = usersRepository.save(user);
        
        return restaurantUserMapper.mapToResponse(updatedUser);
    }
    

    private void validateAdminRestaurantAccess(String adminEmail, Long restaurantId) {
        RestaurantUsers admin = usersRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));
        
        // Check if admin has permission to create users
        if (admin.getRole() != Roles.ADMIN && admin.getRole() != Roles.SUPER_ADMIN) {
            throw new UnauthorizedException("Insufficient permissions to create users");
        }
        
        // Check if admin belongs to the same restaurant (unless SUPER_ADMIN)
        if (admin.getRole() != Roles.SUPER_ADMIN && !admin.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new UnauthorizedException("Cannot create users for different restaurant");
        }
        
        // Verify restaurant exists
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found with id: " + restaurantId);
        }
    }


    private void validateAdminPermissions(String adminEmail) {
        RestaurantUsers admin = usersRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));
    
        if (!admin.getRole().equals(Roles.ADMIN) && !admin.getRole().equals(Roles.SUPER_ADMIN)) {
            throw new UnauthorizedException("Insufficient permissions to update users");
        }
    }

}

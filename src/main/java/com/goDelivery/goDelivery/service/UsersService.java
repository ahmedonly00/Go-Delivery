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

import java.time.LocalDate;
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
        // Get admin user for validation
        RestaurantUsers admin = usersRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));
        
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
        
        // Validate role permissions - RESTAURANT_ADMIN can only create CASHIER and BIKER
        if (admin.getRole() == Roles.RESTAURANT_ADMIN) {
            if (request.getRole() != Roles.CASHIER && request.getRole() != Roles.BIKER) {
                throw new UnauthorizedException("Restaurant admins can only create CASHIER and BIKER users");
            }
        }
       
        // Set default permissions based on role if not provided
        if (request.getPermissions() == null || request.getPermissions().isBlank()) {
            request.setPermissions(getDefaultPermissionsForRole(request.getRole()));
        }
       
        // Map to entity
        RestaurantUsers user = restaurantUserMapper.mapToEntity(request);
        
        // Set restaurant relationship
        user.setRestaurant(restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + request.getRestaurantId())));
        
        // Encode password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Set audit fields
        user.setCreatedAt(LocalDate.now());
        user.setUpdatedAt(LocalDate.now());
        
        // Mark as active and email verified (since created by admin)
        user.setActive(true);
        user.setEmailVerified(true);
        
        // Save user
        RestaurantUsers savedUser = usersRepository.save(user);
        
        return restaurantUserMapper.mapToResponse(savedUser);
    }


    public RestaurantUserResponse updateUser(Long userId, RestaurantUserRequest request, String adminEmail) {
        validateAdminPermissions(adminEmail);
        
        // Get admin user for validation
        RestaurantUsers admin = usersRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));
        
        RestaurantUsers user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Ensure RESTAURANT_ADMIN can only modify users from their own restaurant
        if (admin.getRole() == Roles.RESTAURANT_ADMIN) {
            if (user.getRestaurant() == null || 
                !user.getRestaurant().getRestaurantId().equals(admin.getRestaurant().getRestaurantId())) {
                throw new UnauthorizedException("Cannot modify users from different restaurant");
            }
        }
    
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
        user.setUpdatedAt(LocalDate.now());
        
        RestaurantUsers updatedUser = usersRepository.save(user);
    
        return restaurantUserMapper.mapToResponse(updatedUser);
    }


    public void deleteUser(Long userId, String adminEmail) {
        validateAdminPermissions(adminEmail);
        
        // Get admin user for validation
        RestaurantUsers admin = usersRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));
        
        RestaurantUsers user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Ensure RESTAURANT_ADMIN can only delete users from their own restaurant
        if (admin.getRole() == Roles.RESTAURANT_ADMIN) {
            if (user.getRestaurant() == null || 
                !user.getRestaurant().getRestaurantId().equals(admin.getRestaurant().getRestaurantId())) {
                throw new UnauthorizedException("Cannot delete users from different restaurant");
            }
        }
        
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
        
        // Get admin user for role validation
        RestaurantUsers admin = usersRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));
        
        // Validate role permissions - RESTAURANT_ADMIN can only assign CASHIER and BIKER roles
        if (admin.getRole() == Roles.RESTAURANT_ADMIN) {
            if (role != Roles.CASHIER && role != Roles.BIKER) {
                throw new UnauthorizedException("Restaurant admins can only assign CASHIER and BIKER roles");
            }
        }
        
        RestaurantUsers user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Ensure RESTAURANT_ADMIN can only modify users from their own restaurant
        if (admin.getRole() == Roles.RESTAURANT_ADMIN) {
            if (user.getRestaurant() == null || 
                !user.getRestaurant().getRestaurantId().equals(admin.getRestaurant().getRestaurantId())) {
                throw new UnauthorizedException("Cannot modify users from different restaurant");
            }
        }
        
        user.setRole(role);
        user.setUpdatedAt(LocalDate.now());
        RestaurantUsers updatedUser = usersRepository.save(user);
        
        return restaurantUserMapper.mapToResponse(updatedUser);
    }


    public RestaurantUserResponse deactivateUser(Long userId, String adminEmail) {
        validateAdminPermissions(adminEmail);
        
        // Get admin user for validation
        RestaurantUsers admin = usersRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));
        
        RestaurantUsers user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Ensure RESTAURANT_ADMIN can only deactivate users from their own restaurant
        if (admin.getRole() == Roles.RESTAURANT_ADMIN) {
            if (user.getRestaurant() == null || 
                !user.getRestaurant().getRestaurantId().equals(admin.getRestaurant().getRestaurantId())) {
                throw new UnauthorizedException("Cannot deactivate users from different restaurant");
            }
        }
        
        user.setActive(false);
        user.setUpdatedAt(LocalDate.now());
        RestaurantUsers updatedUser = usersRepository.save(user);
        
        return restaurantUserMapper.mapToResponse(updatedUser);
    }


    public RestaurantUserResponse activateUser(Long userId, String adminEmail) {
        validateAdminPermissions(adminEmail);
        
        // Get admin user for validation
        RestaurantUsers admin = usersRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));
        
        RestaurantUsers user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Ensure RESTAURANT_ADMIN can only activate users from their own restaurant
        if (admin.getRole() == Roles.RESTAURANT_ADMIN) {
            if (user.getRestaurant() == null || 
                !user.getRestaurant().getRestaurantId().equals(admin.getRestaurant().getRestaurantId())) {
                throw new UnauthorizedException("Cannot activate users from different restaurant");
            }
        }
        
        user.setActive(true);
        user.setUpdatedAt(LocalDate.now());
        RestaurantUsers updatedUser = usersRepository.save(user);
        
        return restaurantUserMapper.mapToResponse(updatedUser);
    }
    

    private void validateAdminRestaurantAccess(String adminEmail, Long restaurantId) {
        RestaurantUsers admin = usersRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));
        
        // Check if admin has permission to create users
        if (admin.getRole() != Roles.RESTAURANT_ADMIN && 
            admin.getRole() != Roles.SUPER_ADMIN) {
            throw new UnauthorizedException("Insufficient permissions to create users");
        }
        
        // Check if admin belongs to the same restaurant (unless SUPER_ADMIN)
        if (admin.getRole() != Roles.SUPER_ADMIN && 
            (admin.getRestaurant() == null || !admin.getRestaurant().getRestaurantId().equals(restaurantId))) {
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
    
        if (!admin.getRole().equals(Roles.RESTAURANT_ADMIN) && 
            !admin.getRole().equals(Roles.SUPER_ADMIN)) {
            throw new UnauthorizedException("Insufficient permissions to manage users");
        }
    }
    
    private String getDefaultPermissionsForRole(Roles role) {
        switch (role) {
            case CASHIER:
                return "READ_ORDERS,UPDATE_ORDERS,PROCESS_PAYMENTS";
            case BIKER:
                return "READ_DELIVERIES,UPDATE_DELIVERIES";
            case RESTAURANT_ADMIN:
                return "FULL_ACCESS";
            case SUPER_ADMIN:
                return "SUPER_ACCESS";
            default:
                return "READ_ONLY";
        }
    }

}

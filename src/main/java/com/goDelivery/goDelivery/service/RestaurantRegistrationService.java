package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.RestaurantSetupStatus;
import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.dtos.restaurant.OperatingHoursDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantAdminRegistrationDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantAdminResponseDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantDTO;
import com.goDelivery.goDelivery.exception.ResourceAlreadyExistsException;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.OperatingHoursMapper;
import com.goDelivery.goDelivery.mapper.RestaurantUserMapper;
import com.goDelivery.goDelivery.model.OperatingHours;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.service.email.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantRegistrationService {
    
    private final RestaurantUsersRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantUserMapper userMapper;
    private final OperatingHoursMapper operatingHoursMapper;
    private final EmailService emailService;


    @Transactional
    public RestaurantDTO completeRestaurantRegistration(String username, RestaurantDTO restaurantDTO) {
        // Get the admin user
        RestaurantUsers admin = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
                
        // Check if admin already has a restaurant
        if (admin.getRestaurant() != null) {
            throw new ResourceAlreadyExistsException("Admin already has a registered restaurant");
        }
        
        // Create and save the restaurant
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantName(restaurantDTO.getRestaurantName());
        restaurant.setCuisineType(restaurantDTO.getCuisineType());
        restaurant.setLocation(restaurantDTO.getLocation());
        restaurant.setPhoneNumber(restaurantDTO.getPhoneNumber());
        restaurant.setEmail(restaurantDTO.getEmail());
        restaurant.setLogoUrl(restaurantDTO.getLogoUrl());
        restaurant.setSetupStatus(RestaurantSetupStatus.COMPLETED);
        
        // Convert and set operating hours if present
        if (restaurantDTO.getOperatingHours() != null) {
            OperatingHours operatingHours = operatingHoursMapper.toEntity(restaurantDTO.getOperatingHours());
            restaurant.setOperatingHours(operatingHours);
        }
        
        // Set the admin reference in restaurant
        restaurant.setRestaurantUsers(List.of(admin));
        
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        
        // Update admin's restaurant reference and mark setup as complete
        admin.setRestaurant(savedRestaurant);
        admin.setSetupComplete(true);
        userRepository.save(admin);
        
        // Convert to DTO and return
        return RestaurantDTO.builder()
                .restaurantId(savedRestaurant.getRestaurantId())
                .restaurantName(savedRestaurant.getRestaurantName())
                .description(savedRestaurant.getDescription())
                .cuisineType(savedRestaurant.getCuisineType())
                .location(savedRestaurant.getLocation())
                .phoneNumber(savedRestaurant.getPhoneNumber())
                .email(savedRestaurant.getEmail())
                .build();
    }
    
    @Transactional
    public RestaurantAdminResponseDTO registerRestaurantAdmin(RestaurantAdminRegistrationDTO registrationDTO) {
        // Check if email already exists
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already in use");
        }
        
        // Create admin user without restaurant association
        RestaurantUsers admin = RestaurantUsers.builder()
                .fullName(registrationDTO.getFullName())
                .email(registrationDTO.getEmail().toLowerCase())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .phoneNumber(registrationDTO.getPhoneNumber())
                .isActive(true)
                .emailVerified(false) // Will be verified via email
                .role(Roles.RESTAURANT_ADMIN) // Set the role
                .permissions("RESTAURANT:READ,RESTAURANT:WRITE") // Basic permissions
                .build();
        
        admin = userRepository.save(admin);
        log.info("New restaurant admin registered: {}", admin.getEmail());
        
        // Send verification email (optional)
        try {
            emailService.sendVerificationEmail(admin.getEmail(), admin.getFullName(), "verification-token");
        } catch (Exception e) {
            log.error("Failed to send verification email: {}", e.getMessage());
            // Don't fail the registration if email sending fails
        }
        
        return userMapper.toAdminResponseDTO(admin);
    }
    
}

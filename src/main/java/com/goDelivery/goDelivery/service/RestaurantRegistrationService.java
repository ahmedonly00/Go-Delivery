package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.RestaurantSetupStatus;
import com.goDelivery.goDelivery.Enum.Roles;
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

import java.time.LocalDate;
import java.util.ArrayList;

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
        restaurant.setDescription(restaurantDTO.getDescription());
        restaurant.setPhoneNumber(restaurantDTO.getPhoneNumber());
        restaurant.setEmail(restaurantDTO.getEmail());
        restaurant.setLogoUrl(restaurantDTO.getLogoUrl());
        restaurant.setSetupStatus(RestaurantSetupStatus.COMPLETED);
        
        // Convert and set operating hours if present
        if (restaurantDTO.getOperatingHours() != null) {
            OperatingHours operatingHours = operatingHoursMapper.toEntity(restaurantDTO.getOperatingHours());
            restaurant.setOperatingHours(operatingHours);
        }
        

        // Save the restaurant first to generate an ID
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        
        // Update admin's restaurant reference and mark setup as complete
        admin.setRestaurant(savedRestaurant);
        admin.setSetupComplete(true);
        admin.setEmailVerified(true); // Mark email as verified when restaurant setup is complete

        // Add admin to restaurant's users list
        savedRestaurant.setRestaurantUsers(new ArrayList<>());
        savedRestaurant.getRestaurantUsers().add(admin);

        // Save the updated admin and restaurant
        userRepository.save(admin);
        restaurantRepository.save(savedRestaurant);
    
        // Send "under review" email instead of OTP
        // Email is now verified, so send the "under review" notification
        try {
            log.info("Sending 'under review' email to restaurant admin: {}", admin.getEmail());
            emailService.sendRestaurantUnderReviewEmail(
                admin.getEmail(), 
                admin.getFullName(), 
                savedRestaurant.getRestaurantName()
            );
            log.info("✅ Under review email successfully sent to: {}", admin.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send under review email to {}: {}", admin.getEmail(), e.getMessage(), e);
        }
        
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

        //Check if password is strong
        if (!registrationDTO.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter, one lowercase letter, and one number");
        }

        //Check if phone number is valid
        if (!registrationDTO.getPhoneNumber().matches("^\\d{10,15}$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        
        // Create admin user without restaurant association
        RestaurantUsers admin = RestaurantUsers.builder()
                .fullName(registrationDTO.getFullName())
                .email(registrationDTO.getEmail().toLowerCase())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .phoneNumber(registrationDTO.getPhoneNumber())
                .isActive(true)
                .lastLogin(LocalDate.now())
                .emailVerified(false) // Will be verified via OTP
                .role(Roles.RESTAURANT_ADMIN) // Set the role
                .permissions("RESTAURANT:READ,RESTAURANT:WRITE") // Basic permissions
                .build();
        
        admin = userRepository.save(admin);
        log.info("New restaurant admin registered: {}", admin.getEmail());
        
        // COMMENTED OUT: Welcome email will not be sent during registration
        // Instead, "under review" email will be sent after restaurant setup completion
        // try {
        //     emailService.sendWelcomeEmail(admin.getEmail(), admin.getFullName(), "");
        //     log.info("Welcome email sent to new restaurant admin: {}", admin.getEmail());
        // } catch (Exception e) {
        //     log.error("Failed to send welcome email: {}", e.getMessage());
        // }
        
        return userMapper.toAdminResponseDTO(admin);
    }
    
}

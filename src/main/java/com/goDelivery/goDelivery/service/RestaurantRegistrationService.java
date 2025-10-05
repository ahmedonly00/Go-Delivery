package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.RestaurantSetupStatus;
import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.dtos.restaurant.OperatingHoursDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantAdminRegistrationDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantAdminResponseDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantBasicInfoDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantSettingsDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantSetupProgressDTO;
import com.goDelivery.goDelivery.exception.ResourceAlreadyExistsException;
import com.goDelivery.goDelivery.model.OperatingHours;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.mapper.RestaurantUserMapper;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.service.email.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

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
    private final RestaurantMapper restaurantMapper;
    private final EmailService emailService;


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
    
    @Transactional
    public RestaurantDTO createRestaurant(String adminEmail, RestaurantDTO restaurantDTO) {
        // Find the admin user
        RestaurantUsers admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found with email: " + adminEmail));
                
        // Check if admin already has a restaurant
        if (admin.getRestaurant() != null) {
            throw new IllegalStateException("This admin is already associated with a restaurant");
        }
        
        // Validate required fields
        if (restaurantDTO.getRestaurantName() == null || restaurantDTO.getRestaurantName().trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant name is required");
        }
        
        if (restaurantDTO.getCuisineType() == null || restaurantDTO.getCuisineType().trim().isEmpty()) {
            throw new IllegalArgumentException("Cuisine type is required");
        }
        
        if (restaurantDTO.getLocation() == null || restaurantDTO.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }
        
        // Set default values if not provided
        if (restaurantDTO.getPhoneNumber() == null || restaurantDTO.getPhoneNumber().trim().isEmpty()) {
            restaurantDTO.setPhoneNumber(admin.getPhoneNumber());
        }
        
        if (restaurantDTO.getDeliveryFee() == null) {
            restaurantDTO.setDeliveryFee(0.0f);
        } else if (restaurantDTO.getDeliveryFee() < 0) {
            throw new IllegalArgumentException("Delivery fee cannot be negative");
        }
        
        if (restaurantDTO.getMinimumOrderAmount() == null) {
            restaurantDTO.setMinimumOrderAmount(0.0f);
        } else if (restaurantDTO.getMinimumOrderAmount() < 0) {
            throw new IllegalArgumentException("Minimum order amount cannot be negative");
        }
        
        if (restaurantDTO.getAveragePreparationTime() == null) {
            restaurantDTO.setAveragePreparationTime(30); // Default to 30 minutes
        }
        
        // Create and save the restaurant with all required fields
        Restaurant restaurant = restaurantMapper.toRestaurant(restaurantDTO);
        restaurant.setEmail(adminEmail); // Use admin's email for the restaurant
        restaurant.setIsActive(true);
        restaurant.setCreatedAt(LocalDate.now());
        restaurant.setUpdatedAt(LocalDate.now());
        
        // Set default setup status for new restaurant
        restaurant.setSetupStatus(RestaurantSetupStatus.BASIC_INFO_ADDED);
        restaurant.setSetupProgress(calculateSetupProgress(RestaurantSetupStatus.BASIC_INFO_ADDED));
        
        // Save the restaurant
        restaurant = restaurantRepository.save(restaurant);
        
        // Associate the restaurant with the admin
        admin.setRestaurant(restaurant);
        userRepository.save(admin);
        
        log.info("New restaurant created by admin: {}", adminEmail);
        return restaurantMapper.toRestaurantDTO(restaurant);
    }


    public RestaurantDTO saveBasicInfo(String email, RestaurantBasicInfoDTO basicInfoDTO) {
        RestaurantUsers user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Restaurant restaurant = user.getRestaurant();
        restaurant.setRestaurantName(basicInfoDTO.getRestaurantName());
        restaurant.setDescription(basicInfoDTO.getDescription());
        restaurant.setCuisineType(basicInfoDTO.getCuisineType());
        restaurant.setPhoneNumber(basicInfoDTO.getPhoneNumber());
        restaurant.setLocation(basicInfoDTO.getLocation());
        
        // Update setup status to BASIC_INFO_ADDED
        restaurant.setSetupStatus(RestaurantSetupStatus.BASIC_INFO_ADDED);
        
        // Save the restaurant
        restaurant = restaurantRepository.save(restaurant);
        
        // Calculate setup progress
        int progress = calculateSetupProgress(restaurant.getSetupStatus());
        restaurant.setSetupProgress(progress);
        
        return restaurantMapper.toRestaurantDTO(restaurant);
    }
    
    @Transactional
    public RestaurantDTO completeSetup(String email) {
        RestaurantUsers user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
            
        Restaurant restaurant = user.getRestaurant();
        
        // Validate that all required setup steps are completed
        if (restaurant.getSetupStatus().ordinal() < RestaurantSetupStatus.MENU_SETUP_COMPLETED.ordinal()) {
            throw new IllegalStateException("Cannot complete setup. Please complete all required setup steps first.");
        }
        
        // Update status to COMPLETED
        restaurant.setSetupStatus(RestaurantSetupStatus.COMPLETED);
        restaurant.setSetupProgress(100);
        restaurant.setIsActive(true);
        
        // Save the updated restaurant
        restaurant = restaurantRepository.save(restaurant);
        
        // Send completion email
        emailService.sendSetupCompletionEmail(
            user.getEmail(),
            user.getFullName(),
            restaurant.getRestaurantName()
        );
        return restaurantMapper.toRestaurantDTO(restaurant);
    }
    
    private int calculateSetupProgress(RestaurantSetupStatus status) {
        if (status == null) return 0;
        
        return switch (status) {
            case ACCOUNT_CREATED -> 10;
            case EMAIL_VERIFIED -> 20;
            case BASIC_INFO_ADDED -> 30;
            case LOCATION_ADDED -> 40;
            case SETTINGS_CONFIGURED -> 50;
            case OPERATING_HOURS_ADDED -> 60;
            case BRANDING_ADDED -> 70;
            case MENU_SETUP_STARTED -> 80;
            case MENU_SETUP_COMPLETED -> 90;
            case COMPLETED, ACTIVE -> 100;
            case REJECTED, SUSPENDED -> 0;
            default -> 0;
        };
    }

    @Transactional
    public RestaurantDTO saveSettings(String email, RestaurantSettingsDTO settingsDTO) {
        RestaurantUsers user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Restaurant restaurant = user.getRestaurant();
        
        // Map OperatingHoursDTO to OperatingHours entity
        OperatingHours operatingHours = new OperatingHours();
        OperatingHoursDTO operatingHoursDTO = settingsDTO.getOperatingHours();
        
        // Map the operating hours fields
        operatingHours.setMondayOpen(operatingHoursDTO.getOpen());
        operatingHours.setMondayClose(operatingHoursDTO.getClose());
        // Set the same hours for all days (you can modify this based on your requirements)
        operatingHours.setTuesdayOpen(operatingHoursDTO.getOpen());
        operatingHours.setTuesdayClose(operatingHoursDTO.getClose());
        operatingHours.setWednesdayOpen(operatingHoursDTO.getOpen());
        operatingHours.setWednesdayClose(operatingHoursDTO.getClose());
        operatingHours.setThursdayOpen(operatingHoursDTO.getOpen());
        operatingHours.setThursdayClose(operatingHoursDTO.getClose());
        operatingHours.setFridayOpen(operatingHoursDTO.getOpen());
        operatingHours.setFridayClose(operatingHoursDTO.getClose());
        operatingHours.setSaturdayOpen(operatingHoursDTO.getOpen());
        operatingHours.setSaturdayClose(operatingHoursDTO.getClose());
        operatingHours.setSundayOpen(operatingHoursDTO.getOpen());
        operatingHours.setSundayClose(operatingHoursDTO.getClose());
        
        // Set the restaurant reference on operating hours
        operatingHours.setRestaurant(restaurant);
        
        // Set the operating hours on the restaurant
        restaurant.setOperatingHours(operatingHours);
                
        restaurant = restaurantRepository.save(restaurant);
        return restaurantMapper.toRestaurantDTO(restaurant);
    }

    public RestaurantSetupProgressDTO getSetupProgress(String email) {
        RestaurantUsers user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Restaurant restaurant = user.getRestaurant();
        RestaurantSetupProgressDTO progressDTO = new RestaurantSetupProgressDTO();
        
        // Calculate completion percentage based on setup status
        int progress = switch (restaurant.getSetupStatus()) {
            case ACCOUNT_CREATED -> 10;
            case EMAIL_VERIFIED -> 30;
            case BASIC_INFO_ADDED -> 50;
            case LOCATION_ADDED -> 60;
            case OPERATING_HOURS_ADDED -> 70;
            case BRANDING_ADDED -> 80;
            case SETTINGS_CONFIGURED -> 90;
            case MENU_SETUP_STARTED -> 95;
            case MENU_SETUP_COMPLETED, COMPLETED, ACTIVE -> 100;
            case REJECTED, SUSPENDED -> 0; // Reset progress for these states
            default -> 0; // Default case for any future enum values
        };
        
        progressDTO.setOverallProgress(progress);
        return progressDTO;
    }
}

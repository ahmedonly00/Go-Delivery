package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.RestaurantSetupStatus;
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
        // Map DTO to entity
        RestaurantUsers admin = new RestaurantUsers();
        admin.setFullName(registrationDTO.getFullName());
        admin.setEmail(registrationDTO.getEmail().toLowerCase());
        admin.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        // Set user as active and verified
        admin.setActive(true);
        admin.setEmailVerified(true);

        // Create and save the restaurant first
        Restaurant restaurant = new Restaurant();
        restaurant.setSetupStatus(RestaurantSetupStatus.ACCOUNT_CREATED);
        restaurant = restaurantRepository.save(restaurant);

        try {
            // Now set the restaurant to the admin and save
            admin.setRestaurant(restaurant);
            admin = userRepository.save(admin);
        } catch (Exception e) {
            // If user save fails, delete the restaurant to avoid orphaned records
            restaurantRepository.delete(restaurant);
            throw e;
        }

        log.info("New restaurant admin registered: {}", admin.getEmail());
        return userMapper.toAdminResponseDTO(admin);
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
        return switch (status) {
            case ACCOUNT_CREATED -> 10;
            case EMAIL_VERIFIED -> 20;
            case BASIC_INFO_ADDED -> 30;
            case LOCATION_ADDED -> 50;
            case OPERATING_HOURS_ADDED -> 70;
            case SETTINGS_CONFIGURED -> 80;
            case MENU_SETUP_STARTED -> 85;
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

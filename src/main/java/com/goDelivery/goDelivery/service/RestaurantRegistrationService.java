package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.RestaurantSetupStatus;
import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.dtos.restaurant.OperatingHoursDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantAdminRegistrationDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantAdminResponseDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantBasicInfoDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantSettingsDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantSetupProgressDTO;
import com.goDelivery.goDelivery.exception.ResourceAlreadyExistsException;
import com.goDelivery.goDelivery.model.MenuItem;
import com.goDelivery.goDelivery.model.MenuCategory;
import com.goDelivery.goDelivery.repository.MenuItemRepository;
import com.goDelivery.goDelivery.repository.MenuCategoryRepository;
import com.goDelivery.goDelivery.model.OperatingHours;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.mapper.RestaurantUserMapper;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.service.email.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
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
        admin.setRole(Roles.RESTAURANT_ADMIN);
        admin.setEmailVerified(false);
        admin.setVerificationToken(UUID.randomUUID().toString());
        admin.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

        // Create a new restaurant with default values
        // Restaurant name will be set in the saveBasicInfo step
        Restaurant restaurant = new Restaurant();
        restaurant.setSetupStatus(RestaurantSetupStatus.ACCOUNT_CREATED);
        restaurant = restaurantRepository.save(restaurant);

        // Associate the restaurant with the admin
        admin.setRestaurant(restaurant);
        admin = userRepository.save(admin);

        // Send verification email
        emailService.sendVerificationEmail(
            admin.getEmail(),
            admin.getFullName(),
            admin.getVerificationToken()
        );

        log.info("New restaurant admin registered: {}", admin.getEmail());
        return userMapper.toAdminResponseDTO(admin);
    }

    @Transactional
    public boolean verifyEmail(String token) {
        RestaurantUsers user = userRepository.findByVerificationToken(token)
            .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        user = userRepository.save(user);

        // Get the associated restaurant
        Restaurant restaurant = user.getRestaurant();
        if (restaurant != null) {
            restaurant.setSetupStatus(RestaurantSetupStatus.EMAIL_VERIFIED);
            restaurant = restaurantRepository.save(restaurant);
        }

        // Send welcome email
        emailService.sendWelcomeEmail(
            user.getEmail(),
            user.getFullName(),
            restaurant != null ? restaurant.getRestaurantName() : "Your Restaurant"
        );

        log.info("Email verified for user: {}", user.getEmail());
        return true;
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        RestaurantUsers user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        // Generate new token
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        user = userRepository.save(user);

        // Resend verification email
        emailService.sendVerificationEmail(
            user.getEmail(),
            user.getFullName(),
            user.getVerificationToken()
        );

        log.info("Verification email resent to: {}", user.getEmail());
    }

    @Transactional
    public RestaurantDTO saveBasicInfo(String email, RestaurantBasicInfoDTO basicInfoDTO) {
        RestaurantUsers user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Restaurant restaurant = user.getRestaurant();
        restaurant.setRestaurantName(basicInfoDTO.getRestaurantName());
        restaurant.setDescription(basicInfoDTO.getDescription());
        restaurant.setCuisineType(basicInfoDTO.getCuisineType());
        restaurant.setPhoneNumber(basicInfoDTO.getPhoneNumber());
        restaurant.setLocation(basicInfoDTO.getLocation());
        restaurant.setLogoUrl(basicInfoDTO.getLogoUrl());
        
        // Save the restaurant first to get an ID
        restaurant = restaurantRepository.save(restaurant);
        
        // Create a default category for the restaurant
        MenuCategory defaultCategory = MenuCategory.builder()
                .categoryName("Main Menu")
                .description("Main menu items")
                .image("")
                .sortOrder(1)
                .isActive(true)
                .createdAt(LocalDate.now())
                .restaurant(restaurant)
                .build();
        defaultCategory = menuCategoryRepository.save(defaultCategory);
        
        // Save menu items
        if (basicInfoDTO.getMenuItems() != null && !basicInfoDTO.getMenuItems().isEmpty()) {
            for (MenuItemRequest menuItemRequest : basicInfoDTO.getMenuItems()) {
                MenuItem menuItem = new MenuItem();
                menuItem.setMenuItemName(menuItemRequest.getMenuItemName());
                menuItem.setDescription(menuItemRequest.getDescription() != null ? menuItemRequest.getDescription() : "");
                menuItem.setPrice(menuItemRequest.getPrice());
                menuItem.setImage(menuItemRequest.getImage() != null ? menuItemRequest.getImage() : "");
                menuItem.setIngredients(menuItemRequest.getIngredients() != null ? menuItemRequest.getIngredients() : "");
                menuItem.setAvailable(menuItemRequest.isAvailable());
                menuItem.setPreparationTime(menuItemRequest.getPreparationTime() != null ? menuItemRequest.getPreparationTime() : 15);
                menuItem.setPreparationScore(0);
                menuItem.setCreatedAt(LocalDate.now());
                menuItem.setUpdatedAt(LocalDate.now());
                menuItem.setRestaurant(restaurant);
                menuItem.setCategory(defaultCategory);
                
                menuItemRepository.save(menuItem);
            }
        }
        
        return restaurantMapper.toRestaurantDTO(restaurant);
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
        
        // Set the restaurant reference
        operatingHours.setRestaurant(restaurant);
        
        // Update restaurant settings
        restaurant.setOperatingHours(operatingHours);
        restaurant.setMinimumOrderAmount(settingsDTO.getMinimumOrderAmount().floatValue());
        restaurant.setDeliveryFee(settingsDTO.getDeliveryFee().floatValue());
        restaurant.setAveragePreparationTime(settingsDTO.getAveragePreparationTime());
        restaurant.setSetupStatus(RestaurantSetupStatus.SETTINGS_CONFIGURED);
        
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
        };
        
        progressDTO.setOverallProgress(progress);
        return progressDTO;
    }

    @Transactional
    public RestaurantDTO completeSetup(String email) {
        RestaurantUsers user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Restaurant restaurant = user.getRestaurant();
        if (restaurant.getSetupStatus() != RestaurantSetupStatus.SETTINGS_CONFIGURED) {
            throw new IllegalStateException("Cannot complete setup. Please complete all required steps first.");
        }

        restaurant.setSetupStatus(RestaurantSetupStatus.COMPLETED);
        restaurant = restaurantRepository.save(restaurant);

        // Send setup completion email
        emailService.sendSetupCompletionEmail(
            user.getEmail(),
            user.getFullName(),
            restaurant.getRestaurantName()
        );

        log.info("Restaurant setup completed: {}", restaurant.getRestaurantName());
        return restaurantMapper.toRestaurantDTO(restaurant);
    }
}

package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.ApplicationStatus;
import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationRequest;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationResponse;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.RestaurantApplicationMapper;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantApplicationService {

    private final RestaurantApplicationRepository applicationRepository;
    private final RestaurantRepository restaurantRepository;
    private final SuperAdminRepository adminRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private final RestaurantApplicationMapper applicationMapper;
    private final EmailService emailService;

    @Transactional
    public RestaurantApplicationResponse submitApplication(RestaurantApplicationRequest request) {
        // Validate email doesn't exist in applications or restaurants
        if (applicationRepository.existsByEmail(request.getEmail()) || 
            restaurantRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }

        // Map DTO to entity
        RestaurantApplication application = applicationMapper.toEntity(request);
        application.setApplicationStatus(ApplicationStatus.PENDING);
        application.setAppliedAt(LocalDate.now());
        
        // Save application
        RestaurantApplication savedApplication = applicationRepository.save(application);
        log.info("New restaurant application submitted: {}", savedApplication.getApplicationId());
        
        return applicationMapper.toResponse(savedApplication);
    }

    @Transactional(readOnly = true)
    public RestaurantApplicationResponse getApplicationById(Long applicationId) {
        RestaurantApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));
        return applicationMapper.toResponse(application);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantApplicationResponse> getAllApplications(String status, Pageable pageable) {
        if (status != null) {
            ApplicationStatus applicationStatus = ApplicationStatus.valueOf(status.toUpperCase());
            return applicationRepository.findByApplicationStatus(applicationStatus, pageable)
                .map(applicationMapper::toResponse);
        }
        return applicationRepository.findAll(pageable)
            .map(applicationMapper::toResponse);
    }

    @Transactional
    public RestaurantApplicationResponse reviewApplication(Long applicationId, RestaurantApplicationRequest request) {
        // Find application
        RestaurantApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));
        
        // Update application from request
        applicationMapper.updateFromRequest(application, request);
        application.setReviewedAt(LocalDate.now());
        
        // If reviewer ID is provided, set the reviewer
        if (request.getReviewedById() != null) {
            SuperAdmin reviewer = adminRepository.findById(request.getReviewedById())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with id: " + request.getReviewedById()));
            application.setReviewedBy(reviewer);
        }
        
        // If approved, create restaurant and admin account
        if (request.getApplicationStatus() == ApplicationStatus.APPROVED) {
            createRestaurantAndAdmin(application);
            application.setApprovedAt(LocalDate.now());
        }
        
        RestaurantApplication updatedApplication = applicationRepository.save(application);
        log.info("Application {} reviewed with status: {}", applicationId, request.getApplicationStatus());
            
        return applicationMapper.toResponse(updatedApplication);
    }

    @Transactional(readOnly = true)
    public RestaurantApplicationResponse checkApplicationStatus(String email) {
        RestaurantApplication application = applicationRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("No application found with email: " + email));
        return applicationMapper.toResponse(application);
    }

    @Transactional
    protected void createRestaurantAndAdmin(RestaurantApplication application) {
        // Create restaurant
        Restaurant restaurant = Restaurant.builder()
                .restaurantName(application.getBusinessName())
                .email(application.getEmail())
                .location(application.getLocation())
                .isActive(true)
                .application(application)
                .cuisineType("")
                .phoneNumber("")
                .logo("")
                .rating(0.0f)
                .totalReviews(0)
                .totalOrders(0)
                .averagePreparationTime(0)
                .deliveryFee(0.0f)
                .minimumOrderAmount(0.0f)
                .build();
        
        // Save restaurant
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        
        // Create restaurant admin user
        String tempPassword = generateTemporaryPassword();
        RestaurantUsers adminUser = RestaurantUsers.builder()
                .fullNames(application.getBusinessName() + " Admin")
                .email(application.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .phoneNumber("")
                .role(Roles.RESTAURANT_ADMIN)
                .permissions("")
                .isActive(true)
                .restaurant(savedRestaurant)
                .application(application)
                .build();
        
        // Save admin user using the inner class implementation
        userService.createUser(adminUser);
        
        // Send welcome email with credentials
        sendWelcomeEmail(application.getEmail(), application.getBusinessName(), 
            application.getEmail(), tempPassword);
    }
    
    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 10);
    }
    
    private void sendWelcomeEmail(String email, String businessName, String username, String password) {
        try {
            emailService.sendWelcomeEmail(email, businessName, username, password);
            log.info("Welcome email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", email, e);
            // We don't throw the exception here to prevent the entire operation from failing
            // just because email sending failed
        }
    }
}

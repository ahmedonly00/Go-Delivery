package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.ApplicationStatus;
import com.goDelivery.goDelivery.Enum.Permissions;
import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.dtos.restaurant.CreateRestaurantApplicationRequest;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantAdminCredentials;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationReviewRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
    private final EmailServiceInterface emailService;
    private final UsersRepository usersRepository;


    public Page<RestaurantApplicationResponse> getAllApplications(String status, Pageable pageable) {
        if (status != null) {
            ApplicationStatus applicationStatus = ApplicationStatus.valueOf(status.toUpperCase());
            return applicationRepository.findByApplicationStatus(applicationStatus, pageable)
                .map(applicationMapper::toResponse);
        }
        return applicationRepository.findAll(pageable)
            .map(applicationMapper::toResponse);
    }


    public RestaurantApplicationResponse getApplicationById(Long id) {
        return applicationRepository.findById(id)
            .map(applicationMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }


    public RestaurantApplicationResponse approveApplication(Long id, String adminEmail) {
        RestaurantApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        // Update application status
        application.setApplicationStatus(ApplicationStatus.APPROVED);
        application.setApprovedAt(LocalDate.now());
        
        // Set reviewed by admin if email is provided
        if (adminEmail != null && !adminEmail.isBlank()) {
            SuperAdmin admin = adminRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));
            application.setReviewedBy(admin);
        }

        // Create restaurant and admin user
        createRestaurantAndAdmin(application);
        
        // Send welcome email with temporary password
        String tempPassword = generateTemporaryPassword();
        emailService.sendRestaurantWelcomeEmail(
            application.getBusinessName(),
            application.getEmail(),
            application.getOwnerName(),
            tempPassword
        );

        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Transactional
    public RestaurantApplicationResponse rejectApplication(Long id, String reason, String adminEmail) {
        RestaurantApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        // Update application status and rejection reason
        application.setApplicationStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(reason);
        application.setReviewedAt(LocalDate.now());
        
        // Set reviewed by admin if email is provided
        if (adminEmail != null && !adminEmail.isBlank()) {
            SuperAdmin admin = adminRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));
            application.setReviewedBy(admin);
        }

        // Send rejection email
        emailService.sendApplicationRejectionEmail(
            application.getBusinessName(),
            application.getEmail(),
            "Your restaurant application has been rejected. Reason: " + reason
        );

        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Transactional
    public RestaurantApplicationResponse reviewApplication(
            Long applicationId,
            RestaurantApplicationReviewRequest request,
            String adminEmail) {

        // Ensure admin email is provided
        if (adminEmail == null || adminEmail.isBlank()) {
            throw new RuntimeException("Admin email is required to review the application");
        }

        try {
            // 1. Find application
            RestaurantApplication application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

            // 2. Validate request status
            if (request.getApplicationStatus() == null) {
                throw new IllegalArgumentException("Application status is required");
            }

            // 3. Get the admin who is reviewing
            SuperAdmin admin = adminRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));

            // 4. Update application details
            application.setApplicationStatus(request.getApplicationStatus());
            application.setReviewNote(request.getReviewNote());
            application.setReviewedAt(LocalDate.now());
            application.setReviewedBy(admin);

            RestaurantAdminCredentials credentials = null;

            // 5. Process based on status
            if (request.getApplicationStatus() == ApplicationStatus.APPROVED) {
                application.setApprovedAt(LocalDate.now());
                application.setRejectionReason(null);

                try {
                    // Create restaurant and admin user, return credentials
                    credentials = createRestaurantAndAdmin(application);

                    // Send approval email (not blocking if it fails)
                    try {
                        emailService.sendRestaurantWelcomeEmail(
                            application.getBusinessName(),
                            application.getEmail(),
                            application.getOwnerName(),
                            credentials.getPassword()
                        );
                    } catch (Exception emailEx) {
                        log.warn("Approval email failed to send for application {}: {}", applicationId, emailEx.getMessage());
                    }

                } catch (Exception e) {
                    log.error("Error creating restaurant/admin for application {}: {}", applicationId, e.getMessage(), e);
                    throw new RuntimeException("Failed to create restaurant/admin: " + e.getMessage(), e);
                }

            } else if (request.getApplicationStatus() == ApplicationStatus.REJECTED) {
                // Rejection reason is required
                if (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
                    throw new IllegalArgumentException("Rejection reason is required when rejecting an application");
                }

                application.setRejectionReason(request.getRejectionReason());
                application.setApprovedAt(null);

                // Send rejection email (not blocking if it fails)
                try {
                    emailService.sendApplicationRejectionEmail(
                            application.getBusinessName(),
                            application.getEmail(),
                            "Your restaurant application has been rejected. Reason: " + request.getRejectionReason()
                    );
                } catch (Exception emailEx) {
                    log.warn("Rejection email failed to send for application {}: {}", applicationId, emailEx.getMessage());
                }
            }

            // 6. Save application
            RestaurantApplication updatedApplication = applicationRepository.save(application);

            // 7. Map response
            RestaurantApplicationResponse response = applicationMapper.toResponse(updatedApplication);

            // 8. Attach credentials if approved
            if (credentials != null) {
                response.setAdminUsername(credentials.getUsername());
                response.setAdminPassword(credentials.getPassword());
            }

            return response;

        } catch (Exception e) {
            log.error("Failed to process application review for {}: {}", applicationId, e.getMessage(), e);
            throw new RuntimeException("Failed to process application review: " + e.getMessage(), e);
        }
    }

    public RestaurantApplicationResponse submitApplication(CreateRestaurantApplicationRequest request) {
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

    public RestaurantApplicationResponse checkApplicationStatus(String email) {
        RestaurantApplication application = applicationRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("No application found with email: " + email));
        return applicationMapper.toResponse(application);
    }

    
    @Transactional
    public RestaurantAdminCredentials createRestaurantAndAdmin(RestaurantApplication application) {
        // Validate input
        if (application == null) {
            throw new IllegalArgumentException("Restaurant application cannot be null");
        }
        
        if (application.getEmail() == null || application.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        String tempPassword = generateTemporaryPassword();
        
        try {
            // Create and save restaurant
            Restaurant restaurant = createAndSaveRestaurant(application);
            
            // Create and save admin user
            createAndSaveAdminUser(application, restaurant, tempPassword);
            
            // Send welcome email (async)
            sendWelcomeEmailAsync(application.getEmail(), application.getBusinessName(), tempPassword);
            
            return new RestaurantAdminCredentials(application.getEmail(), tempPassword);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create restaurant and admin: " + e.getMessage(), e);
        }
    }
    
    private Restaurant createAndSaveRestaurant(RestaurantApplication application) {
        // Create restaurant with all required fields
        Restaurant restaurant = Restaurant.builder()
                .restaurantName(application.getBusinessName())
                .location(application.getLocation())
                .email(application.getEmail())
                .isActive(true)
                .application(application)
                .cuisineType(application.getCuisineType())
                .phoneNumber(application.getPhoneNumber())
                .logoUrl("default-logo.png")
                .bannerUrl("default-banner.png")
                .rating(0.0f)
                .totalReviews(0)
                .totalOrders(0)
                .averagePreparationTime(0)
                .deliveryFee(0.0f)
                .minimumOrderAmount(0.0f)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
        
        // Save restaurant
        return restaurantRepository.save(restaurant);
    }
    
    private void createAndSaveAdminUser(RestaurantApplication application, Restaurant restaurant, String tempPassword) {
        // Create admin user with temporary password
        RestaurantUsers adminUser = new RestaurantUsers();
        adminUser.setFullNames(application.getBusinessName() + " Admin");
        adminUser.setEmail(application.getEmail());
        adminUser.setPassword(passwordEncoder.encode(tempPassword));
        adminUser.setPhoneNumber(application.getPhoneNumber() != null ? application.getPhoneNumber() : "");
        adminUser.setRole(Roles.ADMIN);
        adminUser.setPermissions(Permissions.values().toString());
        adminUser.setActive(true);
        adminUser.setRestaurant(restaurant);
        adminUser.setApplication(application);
        adminUser.setCreatedAt(java.time.LocalDateTime.now());
        adminUser.setUpdatedAt(java.time.LocalDateTime.now());
        
        // Save admin user
        usersRepository.save(adminUser);
    }
    
    private void sendWelcomeEmailAsync(String email, String businessName, String tempPassword) {
        try {
            // Using a separate thread to send email asynchronously
            new Thread(() -> {
                try {
                    emailService.sendRestaurantWelcomeEmail(businessName, email, email, tempPassword);
                } catch (Exception e) {
                    // Log the error but don't throw it since this is an async operation
                    System.err.println("Failed to send welcome email: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            // If async execution fails, log and continue
            System.err.println("Failed to schedule welcome email: " + e.getMessage());
        }
    }
    
    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 10);
    }
}

package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.ApplicationStatus;
import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.dtos.restaurant.CreateRestaurantApplicationRequest;
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
    private final UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private final RestaurantApplicationMapper applicationMapper;
    private final EmailService emailService;

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

    @Transactional(readOnly = true)
    public RestaurantApplicationResponse getApplicationById(Long id) {
        return applicationRepository.findById(id)
            .map(applicationMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    @Transactional
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
        
        // Send approval email
        emailService.sendApplicationStatusEmail(
            application.getEmail(),
            "Restaurant Application Approved",
            application.getBusinessName(),
            "approved",
            "Your restaurant application has been approved. You can now log in to your restaurant dashboard."
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
        emailService.sendApplicationStatusEmail(
            application.getEmail(),
            "Restaurant Application Rejected",
            application.getBusinessName(),
            "rejected",
            "Your restaurant application has been rejected. Reason: " + reason
        );

        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Transactional
    public RestaurantApplicationResponse reviewApplication(Long applicationId, RestaurantApplicationReviewRequest request, String adminEmail) {
        // Find application
        RestaurantApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));
        
        // Set the admin who reviewed the application
        if (adminEmail != null && !adminEmail.isBlank()) {
            SuperAdmin admin = adminRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));
            application.setReviewedBy(admin);
        }
        
        // Update status if provided
        if (request.getApplicationStatus() != null) {
            application.setApplicationStatus(request.getApplicationStatus());
            
            // Set timestamps based on status
            if (request.getApplicationStatus() == ApplicationStatus.APPROVED) {
                application.setApprovedAt(LocalDate.now());
                // Create restaurant and admin user for approved applications
                createRestaurantAndAdmin(application);
            } else if (request.getApplicationStatus() == ApplicationStatus.REJECTED) {
                application.setRejectionReason(request.getRejectionReason());
                application.setReviewedAt(LocalDate.now());
            }
        }
        
        // Save the updated application
        RestaurantApplication updatedApplication = applicationRepository.save(application);
        
        // Send appropriate email notification
        if (request.getApplicationStatus() != null) {
            String status = request.getApplicationStatus().toString().toLowerCase();
            String subject = "Restaurant Application " + status.substring(0, 1).toUpperCase() + status.substring(1);
            String message = request.getApplicationStatus() == ApplicationStatus.REJECTED ? 
                ("Your application has been rejected. Reason: " + request.getRejectionReason()) :
                "Your restaurant application has been approved. You can now log in to your restaurant dashboard.";
                
            emailService.sendApplicationStatusEmail(
                application.getEmail(),
                subject,
                application.getBusinessName(),
                status,
                message
            );
        }
        
        return applicationMapper.toResponse(updatedApplication);
    }
    
    @Transactional
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
                .logoUrl("")
                .bannerUrl("")
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
        }
    }
}

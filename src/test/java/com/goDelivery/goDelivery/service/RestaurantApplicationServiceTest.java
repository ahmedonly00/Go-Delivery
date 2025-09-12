package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.ApplicationStatus;
import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.dtos.restaurant.CreateRestaurantApplicationRequest;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationReviewRequest;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationResponse;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantApplicationServiceTest {

    @Mock
    private RestaurantApplicationRepository applicationRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private SuperAdminRepository adminRepository;

    @Mock
    private RestaurantUsersRepository restaurantUsersRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RestaurantApplicationService applicationService;

    private CreateRestaurantApplicationRequest request;
    private RestaurantApplication application;
    private SuperAdmin admin;

    @BeforeEach
    void setUp() {
        request = CreateRestaurantApplicationRequest.builder()
                .businessName("Test Restaurant")
                .email("test@example.com")
                .location("Test Location")
                .ownerName("Test Owner")
                .phoneNumber("1234567890")
                .cuisineType("Italian")
                .yearsInBusiness(5)
                .description("Test Description")
                .build();

        application = RestaurantApplication.builder()
                .applicationId(1L)
                .businessName("Test Restaurant")
                .email("test@example.com")
                .location("Test Location")
                .applicationStatus(ApplicationStatus.PENDING)
                .appliedAt(LocalDate.now())
                .build();

        admin = SuperAdmin.builder()
                .adminId(1L)
                .fullNames("Admin User")
                .email("admin@example.com")
                .password("password")
                .role(Roles.ADMIN)
                .isActive(true)
                .build();
    }

    @Test
    void submitApplication_ShouldSaveNewApplication() {
        when(applicationRepository.existsByEmail(anyString())).thenReturn(false);
        when(restaurantRepository.existsByEmail(anyString())).thenReturn(false);
        when(applicationRepository.save(any(RestaurantApplication.class))).thenReturn(application);

        RestaurantApplicationResponse response = applicationService.submitApplication(request);

        assertNotNull(response);
        assertEquals("Test Restaurant", response.getBusinessName());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(ApplicationStatus.PENDING, response.getApplicationStatus());
        
        verify(applicationRepository, times(1)).save(any(RestaurantApplication.class));
    }

    @Test
    void getApplicationById_ShouldReturnApplication() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        RestaurantApplicationResponse response = applicationService.getApplicationById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getApplicationId());
        assertEquals("Test Restaurant", response.getBusinessName());
    }

    @Test
    void getAllApplications_ShouldReturnPageOfApplications() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RestaurantApplication> page = new PageImpl<>(Collections.singletonList(application), pageable, 1);
        
        when(applicationRepository.findAll(pageable)).thenReturn(page);
        
        Page<RestaurantApplicationResponse> result = applicationService.getAllApplications(null, pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Restaurant", result.getContent().get(0).getBusinessName());
    }

    @Test
    void reviewApplication_ShouldUpdateApplicationStatus() {
        RestaurantApplicationReviewRequest reviewRequest = RestaurantApplicationReviewRequest.builder()
                .applicationStatus(ApplicationStatus.APPROVED)
                .reviewedById(1L)
                .build();
        
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(applicationRepository.save(any(RestaurantApplication.class))).thenReturn(application);
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(new Restaurant());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        RestaurantApplicationResponse response = applicationService.reviewApplication(1L, reviewRequest, "admin@example.com");
        
        assertNotNull(response);
        verify(applicationRepository, times(1)).save(any(RestaurantApplication.class));
    }

    @Test
    void checkApplicationStatus_ShouldReturnApplicationStatus() {
        when(applicationRepository.findByEmail("test@example.com")).thenReturn(Optional.of(application));
        
        RestaurantApplicationResponse response = applicationService.checkApplicationStatus("test@example.com");
        
        assertNotNull(response);
        assertEquals(ApplicationStatus.PENDING, response.getApplicationStatus());
    }

    @Test
    void checkApplicationStatus_ShouldThrowWhenNotFound() {
        when(applicationRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            applicationService.checkApplicationStatus("nonexistent@example.com");
        });
    }
}

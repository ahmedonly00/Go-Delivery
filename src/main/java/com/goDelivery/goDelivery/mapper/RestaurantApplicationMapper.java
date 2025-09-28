package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.dtos.restaurant.CreateRestaurantApplicationRequest;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationReviewRequest;
import com.goDelivery.goDelivery.Enum.ApplicationStatus;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationResponse;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationResponse.SimpleAdminDto;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationResponse.SimpleRestaurantDto;
import com.goDelivery.goDelivery.model.RestaurantApplication;
import com.goDelivery.goDelivery.model.SuperAdmin;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Component
public class RestaurantApplicationMapper {

    public RestaurantApplicationResponse toResponse(RestaurantApplication application) {
        if (application == null) {
            return null;
        }

        return RestaurantApplicationResponse.builder()
                .applicationId(application.getApplicationId())
                .businessName(application.getBusinessName())
                .email(application.getEmail())
                .location(application.getLocation())
                .applicationStatus(application.getApplicationStatus())
                .reviewNote(application.getReviewNote())
                .ownerName(application.getOwnerName())
                .phoneNumber(application.getPhoneNumber())
                .cuisineType(application.getCuisineType())
                .yearsInBusiness(application.getYearsInBusiness())
                .description(application.getDescription())
                .appliedAt(application.getAppliedAt())
                .reviewedAt(application.getReviewedAt())
                .rejectionReason(application.getRejectionReason())
                .approvedAt(application.getApprovedAt())
                .reviewedBy(mapToSimpleAdminDto(application.getReviewedBy()))
                .restaurant(mapToSimpleRestaurantDto(application.getRestaurant()))
                .adminUsername(application.getRestaurantAdmin() != null ? application.getRestaurantAdmin().getUsername() : null)
                .adminPassword(application.getRestaurantAdmin() != null ? application.getRestaurantAdmin().getPassword() : null)
                .build();
    }

    public RestaurantApplication toEntity(CreateRestaurantApplicationRequest request) {
        if (request == null) {
            return null;
        }

        return RestaurantApplication.builder()
                .businessName(request.getBusinessName())
                .email(request.getEmail())
                .location(request.getLocation())
                .ownerName(request.getOwnerName())
                .phoneNumber(request.getPhoneNumber())
                .cuisineType(request.getCuisineType())
                .yearsInBusiness(request.getYearsInBusiness())
                .description(request.getDescription())
                .appliedAt(LocalDate.now())
                .applicationStatus(ApplicationStatus.PENDING)
                .build();
    }

    public RestaurantApplication updateFromRequest(RestaurantApplication application, RestaurantApplicationReviewRequest request) {
        if (application == null || request == null) {
            return application;
        }

        if (request.getApplicationStatus() != null) {
            application.setApplicationStatus(request.getApplicationStatus());
        }
        if (request.getReviewNote() != null) {
            application.setReviewNote(request.getReviewNote());
        }
        
        if (request.getRejectionReason() != null) {
            application.setRejectionReason(request.getRejectionReason());
        }

        if (request.getReviewedAt() != null) {
            application.setReviewedAt(request.getReviewedAt());
        }
        if (request.getApprovedAt() != null) {
            application.setApprovedAt(request.getApprovedAt());
        }

        return application;
    }

    private SimpleAdminDto mapToSimpleAdminDto(SuperAdmin admin) {
        if (admin == null) {
            return null;
        }
        return SimpleAdminDto.builder()
                .adminId(admin.getAdminId())
                .fullName(admin.getFullNames())
                .email(admin.getEmail())
                .build();
    }

    private SimpleRestaurantDto mapToSimpleRestaurantDto(com.goDelivery.goDelivery.model.Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }
        return SimpleRestaurantDto.builder()
                .restaurantId(restaurant.getRestaurantId())
                .name(restaurant.getRestaurantName())
                .status(restaurant.isActive() ? "ACTIVE" : "INACTIVE")
                .build();
    }
}

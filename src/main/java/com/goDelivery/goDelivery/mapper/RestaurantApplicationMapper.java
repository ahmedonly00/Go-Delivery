package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationRequest;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationResponse;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationResponse.SimpleAdminDto;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationResponse.SimpleRestaurantDto;
import com.goDelivery.goDelivery.model.RestaurantApplication;
import com.goDelivery.goDelivery.model.SuperAdmin;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Mapper class for converting between RestaurantApplication entities and DTOs.
 */
@Component
public class RestaurantApplicationMapper {

    /**
     * Converts a RestaurantApplication entity to a RestaurantApplicationResponse DTO.
     *
     * @param application the entity to convert
     * @return the converted DTO
     */
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
                .rejectionReason(application.getRejectionReason())
                .appliedAt(application.getAppliedAt())
                .reviewedAt(application.getReviewedAt())
                .approvedAt(application.getApprovedAt())
                .reviewedBy(mapToSimpleAdminDto(application.getReviewedBy()))
                .restaurant(mapToSimpleRestaurantDto(application.getRestaurant()))
                .build();
    }

    /**
     * Creates a new RestaurantApplication from a RestaurantApplicationRequest.
     *
     * @param request the request DTO
     * @return a new RestaurantApplication entity
     */
    public RestaurantApplication toEntity(RestaurantApplicationRequest request) {
        if (request == null) {
            return null;
        }

        return RestaurantApplication.builder()
                .businessName(request.getBusinessName())
                .email(request.getEmail())
                .location(request.getLocation())
                .applicationStatus(request.getApplicationStatus())
                .rejectionReason(request.getRejectionReason())
                .appliedAt(LocalDate.now())
                .build();
    }

    /**
     * Updates an existing RestaurantApplication from a RestaurantApplicationRequest.
     *
     * @param application the entity to update
     * @param request    the request DTO with updated values
     * @return the updated entity
     */
    public RestaurantApplication updateFromRequest(RestaurantApplication application, RestaurantApplicationRequest request) {
        if (application == null || request == null) {
            return application;
        }

        if (request.getBusinessName() != null) {
            application.setBusinessName(request.getBusinessName());
        }
        if (request.getEmail() != null) {
            application.setEmail(request.getEmail());
        }
        if (request.getLocation() != null) {
            application.setLocation(request.getLocation());
        }
        if (request.getApplicationStatus() != null) {
            application.setApplicationStatus(request.getApplicationStatus());
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

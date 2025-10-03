package com.goDelivery.goDelivery.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.goDelivery.goDelivery.dtos.restaurant.RestaurantAdminResponseDTO;
import com.goDelivery.goDelivery.dtos.user.RestaurantUserRequest;
import com.goDelivery.goDelivery.dtos.user.RestaurantUserResponse;
import com.goDelivery.goDelivery.model.RestaurantUsers;

@Component
public class RestaurantUserMapper {

    public RestaurantUserResponse mapToResponse(RestaurantUsers restaurantUser) {
        
        return RestaurantUserResponse.builder()
                .userId(restaurantUser.getUserId())
                .fullName(restaurantUser.getFullName())
                .email(restaurantUser.getEmail())
                .phoneNumber(restaurantUser.getPhoneNumber())
                .role(restaurantUser.getRole())
                .permissions(restaurantUser.getPermissions())
                .isActive(true)
                .lastLogin(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .restaurantId(restaurantUser.getRestaurant() != null ? restaurantUser.getRestaurant().getRestaurantId() : null)
                .restaurantName(restaurantUser.getRestaurant() != null ? restaurantUser.getRestaurant().getRestaurantName() : null)
                .build();
    }

    public RestaurantUsers mapToEntity(RestaurantUserRequest request) {
        return RestaurantUsers.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .permissions(request.getPermissions())
                .isActive(true)
                .build();
    }
    
    public RestaurantUserRequest mapToRequest(RestaurantUsers user) {
        RestaurantUserRequest request = new RestaurantUserRequest();
        request.setFullName(user.getFullName());
        request.setEmail(user.getEmail());
        request.setPhoneNumber(user.getPhoneNumber());
        request.setRole(user.getRole());
        request.setPermissions(user.getPermissions());
        request.setPassword(user.getPassword()); // Note: This is already encoded
        return request;
    }
    
    public RestaurantAdminResponseDTO toAdminResponseDTO(RestaurantUsers user) {
        return RestaurantAdminResponseDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .setupComplete(false) // Default to false as this is a new registration
                .message("Admin registration successful. Please check your email for verification.")
                .build();
    }
}

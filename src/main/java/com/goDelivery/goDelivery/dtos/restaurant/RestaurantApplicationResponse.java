package com.goDelivery.goDelivery.dtos.restaurant;

import com.goDelivery.goDelivery.Enum.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantApplicationResponse {
    private Long applicationId;
    private String businessName;
    private String email;
    private String location;
    private String ownerName;
    private String phoneNumber;
    private String cuisineType;
    private Integer yearsInBusiness;
    private String description;
    private String logoUrl;
    private ApplicationStatus applicationStatus;
    private String reviewNote;
    private String rejectionReason;
    private LocalDate appliedAt;
    private LocalDate reviewedAt;
    private LocalDate approvedAt;
    
    // Nested DTOs for related entities
    private SimpleAdminDto reviewedBy;
    private SimpleRestaurantDto restaurant;
    private SimpleUserDto restaurantAdmin;

    private String adminUsername;
    private String adminPassword;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleAdminDto {
        private Long adminId;
        private String fullName;
        private String email;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleRestaurantDto {
        private Long restaurantId;
        private String name;
        private String status;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleUserDto {
        private Long userId;
        private String fullName;
        private String email;
    }
}

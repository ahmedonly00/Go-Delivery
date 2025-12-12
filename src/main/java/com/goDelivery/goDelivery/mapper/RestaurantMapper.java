package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantReviewDTO;
import com.goDelivery.goDelivery.model.Branches;
import com.goDelivery.goDelivery.model.Restaurant;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class RestaurantMapper {

    //Convert List of Restaurants to List of RestaurantDTO
    public List<RestaurantDTO> toRestaurantDTO(List<Restaurant> restaurants) {
        if (restaurants == null) {
            return null;
        }
       
        return restaurants.stream()
                .map(restaurant -> RestaurantDTO.builder()
                .restaurantId(restaurant.getRestaurantId())
                .restaurantName(restaurant.getRestaurantName())
                .location(restaurant.getLocation())
                .cuisineType(restaurant.getCuisineType())
                .email(restaurant.getEmail())
                .phoneNumber(restaurant.getPhoneNumber())
                .logoUrl(restaurant.getLogoUrl())
                .description(restaurant.getDescription())
                .rating(restaurant.getRating())
                .totalReviews(restaurant.getTotalReviews())
                .averagePreparationTime(restaurant.getAveragePreparationTime())
                .deliveryFee(restaurant.getDeliveryFee())
                .minimumOrderAmount(restaurant.getMinimumOrderAmount())
                .commercialRegistrationCertificateUrl(restaurant.getCommercialRegistrationCertificateUrl())
                .taxIdentificationNumber(restaurant.getTaxIdentificationNumber())
                .taxIdentificationDocumentUrl(restaurant.getTaxIdentificationDocumentUrl())
                .isApproved(restaurant.getIsApproved())
                .approvalStatus(restaurant.getApprovalStatus())
                .rejectionReason(restaurant.getRejectionReason())
                .reviewedBy(restaurant.getReviewedBy())
                .reviewedAt(restaurant.getReviewedAt())
                .isActive(restaurant.getIsActive())
                .build())
                .collect(Collectors.toList());
    }
    
    //Convert Restaurant to RestaurantDTO
    public RestaurantDTO toRestaurantDTO(Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }
        
        return RestaurantDTO.builder()
                .restaurantId(restaurant.getRestaurantId())
                .restaurantName(restaurant.getRestaurantName())
                .location(restaurant.getLocation())
                .cuisineType(restaurant.getCuisineType())
                .email(restaurant.getEmail())
                .phoneNumber(restaurant.getPhoneNumber())
                .logoUrl(restaurant.getLogoUrl())
                .description(restaurant.getDescription())
                .rating(restaurant.getRating())
                .totalReviews(restaurant.getTotalReviews())
                .averagePreparationTime(restaurant.getAveragePreparationTime())
                .deliveryFee(restaurant.getDeliveryFee())
                .minimumOrderAmount(restaurant.getMinimumOrderAmount())
                .commercialRegistrationCertificateUrl(restaurant.getCommercialRegistrationCertificateUrl())
                .taxIdentificationNumber(restaurant.getTaxIdentificationNumber())
                .taxIdentificationDocumentUrl(restaurant.getTaxIdentificationDocumentUrl())
                .isApproved(restaurant.getIsApproved())
                .approvalStatus(restaurant.getApprovalStatus())
                .rejectionReason(restaurant.getRejectionReason())
                .reviewedBy(restaurant.getReviewedBy())
                .reviewedAt(restaurant.getReviewedAt())
                .isActive(restaurant.getIsActive())
                .build();
    }

    //Convert RestaurantDTO to Restaurant
    // For CREATING new restaurants(no ID mapping)
    public Restaurant toRestaurantForCreate(RestaurantDTO restaurantDTO) {
        if (restaurantDTO == null) {
            return null;
        }

        return Restaurant.builder()
                .restaurantId(restaurantDTO.getRestaurantId())
                .restaurantName(restaurantDTO.getRestaurantName())
                .location(restaurantDTO.getLocation())
                .cuisineType(restaurantDTO.getCuisineType())
                .email(restaurantDTO.getEmail())
                .phoneNumber(restaurantDTO.getPhoneNumber())
                .logoUrl(restaurantDTO.getLogoUrl())
                .description(restaurantDTO.getDescription())
                .rating(restaurantDTO.getRating() != null ? restaurantDTO.getRating() : 0.0f)
                .totalReviews(restaurantDTO.getTotalReviews() != null ? restaurantDTO.getTotalReviews() : 0)
                .averagePreparationTime(restaurantDTO.getAveragePreparationTime() != null ? 
                    restaurantDTO.getAveragePreparationTime() : 30) // Default to 30 minutes
                .deliveryFee(restaurantDTO.getDeliveryFee() != null ? restaurantDTO.getDeliveryFee() : 0.0f)
                .minimumOrderAmount(restaurantDTO.getMinimumOrderAmount() != null ? 
                    restaurantDTO.getMinimumOrderAmount() : 0.0f)
                .commercialRegistrationCertificateUrl(restaurantDTO.getCommercialRegistrationCertificateUrl())
                .taxIdentificationNumber(restaurantDTO.getTaxIdentificationNumber())
                .taxIdentificationDocumentUrl(restaurantDTO.getTaxIdentificationDocumentUrl())
                .isApproved(restaurantDTO.getIsApproved() != null ? restaurantDTO.getIsApproved() : false)
                .approvalStatus(restaurantDTO.getApprovalStatus() != null ? restaurantDTO.getApprovalStatus() : com.goDelivery.goDelivery.Enum.ApprovalStatus.PENDING)
                .rejectionReason(restaurantDTO.getRejectionReason())
                .reviewedBy(restaurantDTO.getReviewedBy())
                .reviewedAt(restaurantDTO.getReviewedAt())
                .isActive(restaurantDTO.isActive())
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
    }

    // For UPDATING existing restaurants (with ID mapping)
    public void toRestaurantForUpdate(Restaurant existingRestaurant, RestaurantDTO restaurantDTO) {

        existingRestaurant.setRestaurantName(restaurantDTO.getRestaurantName());
        existingRestaurant.setLocation(restaurantDTO.getLocation());
        existingRestaurant.setCuisineType(restaurantDTO.getCuisineType());
        existingRestaurant.setEmail(restaurantDTO.getEmail());
        existingRestaurant.setPhoneNumber(restaurantDTO.getPhoneNumber());
        existingRestaurant.setLogoUrl(restaurantDTO.getLogoUrl());
        existingRestaurant.setDescription(restaurantDTO.getDescription());
        existingRestaurant.setAveragePreparationTime(restaurantDTO.getAveragePreparationTime());
        existingRestaurant.setDeliveryFee(restaurantDTO.getDeliveryFee());
        existingRestaurant.setMinimumOrderAmount(restaurantDTO.getMinimumOrderAmount());
        existingRestaurant.setCommercialRegistrationCertificateUrl(restaurantDTO.getCommercialRegistrationCertificateUrl());
        existingRestaurant.setTaxIdentificationNumber(restaurantDTO.getTaxIdentificationNumber());
        existingRestaurant.setTaxIdentificationDocumentUrl(restaurantDTO.getTaxIdentificationDocumentUrl());
        existingRestaurant.setIsActive(restaurantDTO.isActive());
        existingRestaurant.setUpdatedAt(LocalDate.now());
    }

    //Branches
    public List<BranchesDTO> mapBranchesToDTOs(List<Branches> branches) {
        if (branches == null) {
            return null;
        }
        return branches.stream()
                .map(this::toBranchDTO)
                .collect(Collectors.toList());
    }

    //Convert Branches to BranchesDTO
    public BranchesDTO toBranchDTO(Branches branch) {
        if (branch == null) {
            return null;
        }

        return BranchesDTO.builder()
                .branchId(branch.getBranchId())
                .branchName(branch.getBranchName())
                .address(branch.getAddress())
                .latitude(branch.getLatitude())
                .longitude(branch.getLongitude())
                .phoneNumber(branch.getPhoneNumber())
                .operatingHours(branch.getOperatingHours())
                .isActive(branch.isActive())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .restaurantId(branch.getRestaurant() != null ? branch.getRestaurant().getRestaurantId() : null)
                .build();
    }

    //Convert BranchesDTO to Branches
    public Branches toBranch(BranchesDTO branchDTO) {
        if (branchDTO == null) {
            return null;
        }

        return Branches.builder()
                .branchId(branchDTO.getBranchId())
                .branchName(branchDTO.getBranchName())
                .address(branchDTO.getAddress())
                .latitude(branchDTO.getLatitude())
                .longitude(branchDTO.getLongitude())
                .phoneNumber(branchDTO.getPhoneNumber())
                .operatingHours(branchDTO.getOperatingHours())
                .isActive(branchDTO.isActive())
                .createdAt(branchDTO.getCreatedAt() != null ? branchDTO.getCreatedAt() : LocalDate.now())
                .updatedAt(branchDTO.getUpdatedAt() != null ? branchDTO.getUpdatedAt() : LocalDate.now())
                .build();
    }

    // Convert Restaurant to RestaurantReviewDTO (for Super Admin review)
    public RestaurantReviewDTO toRestaurantReviewDTO(Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }

        return RestaurantReviewDTO.builder()
                .restaurantId(restaurant.getRestaurantId())
                .restaurantName(restaurant.getRestaurantName())
                .location(restaurant.getLocation())
                .cuisineType(restaurant.getCuisineType())
                .email(restaurant.getEmail())
                .phoneNumber(restaurant.getPhoneNumber())
                .logoUrl(restaurant.getLogoUrl())
                .description(restaurant.getDescription())
                .deliveryFee(restaurant.getDeliveryFee())
                .minimumOrderAmount(restaurant.getMinimumOrderAmount())
                .averagePreparationTime(restaurant.getAveragePreparationTime())
                .commercialRegistrationCertificateUrl(restaurant.getCommercialRegistrationCertificateUrl())
                .taxIdentificationNumber(restaurant.getTaxIdentificationNumber())
                .taxIdentificationDocumentUrl(restaurant.getTaxIdentificationDocumentUrl())
                .isApproved(restaurant.getIsApproved())
                .approvalStatus(restaurant.getApprovalStatus())
                .rejectionReason(restaurant.getRejectionReason())
                .reviewedBy(restaurant.getReviewedBy())
                .reviewedAt(restaurant.getReviewedAt())
                .createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt())
                .build();
    }

    public List<RestaurantReviewDTO> toRestaurantReviewDTOList(List<Restaurant> restaurants) {
        if (restaurants == null) {
            return null;
        }
        return restaurants.stream()
                .map(this::toRestaurantReviewDTO)
                .collect(Collectors.toList());
    }
}

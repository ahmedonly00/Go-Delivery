package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantDTO;
import com.goDelivery.goDelivery.model.Branches;
import com.goDelivery.goDelivery.model.Restaurant;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class RestaurantMapper {

    public List<RestaurantDTO> toRestaurantDTO(List<Restaurant> restaurants) {
        if (restaurants == null) {
            return null;
        }
        return restaurants.stream()
                .map(this::toRestaurantDTO)
                .collect(Collectors.toList());
    }
    
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
                .bannerUrl(restaurant.getBannerUrl())
                .rating(restaurant.getRating())
                .totalReviews(restaurant.getTotalReviews())
                .totalOrders(restaurant.getTotalOrders())
                .averagePreparationTime(restaurant.getAveragePreparationTime())
                .deliveryFee(restaurant.getDeliveryFee())
                .minimumOrderAmount(restaurant.getMinimumOrderAmount())
                .isActive(restaurant.isActive())
                .branches(mapBranchesToDTOs(restaurant.getBranches()))
                .build();
    }


    public Restaurant toRestaurant(RestaurantDTO restaurantDTO) {
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
                .bannerUrl(restaurantDTO.getBannerUrl())
                .rating(restaurantDTO.getRating())
                .totalReviews(restaurantDTO.getTotalReviews() != null ? restaurantDTO.getTotalReviews() : 0)
                .totalOrders(restaurantDTO.getTotalOrders() != null ? restaurantDTO.getTotalOrders() : 0)
                .averagePreparationTime(restaurantDTO.getAveragePreparationTime())
                .deliveryFee(restaurantDTO.getDeliveryFee())
                .minimumOrderAmount(restaurantDTO.getMinimumOrderAmount())
                .isActive(restaurantDTO.isActive())
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
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
}

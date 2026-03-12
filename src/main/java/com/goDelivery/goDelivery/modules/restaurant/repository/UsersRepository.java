package com.goDelivery.goDelivery.modules.restaurant.repository;

import com.goDelivery.goDelivery.shared.enums.Roles;
import com.goDelivery.goDelivery.modules.restaurant.model.RestaurantUsers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<RestaurantUsers, Long> {
    
    Optional<RestaurantUsers> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    // Find all users by restaurant ID
    List<RestaurantUsers> findByRestaurant_RestaurantId(Long restaurantId);
    
    // Find active users by restaurant ID
    List<RestaurantUsers> findByRestaurant_RestaurantIdAndIsActiveTrue(Long restaurantId);
    
    // Find users by role and restaurant ID
    List<RestaurantUsers> findByRoleAndRestaurant_RestaurantId(Roles role, Long restaurantId);

    boolean existsByRestaurant_RestaurantIdAndEmail(Long restaurantId, String email);
}

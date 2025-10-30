package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantUsersRepository extends JpaRepository<RestaurantUsers, Long> {
    boolean existsByEmail(String email);
    Optional<RestaurantUsers> findByEmail(String email);
    Optional<RestaurantUsers> findByFullName(String fullName);
    Optional<RestaurantUsers> findByUserId(Long userId);
    
    @Query("SELECT ru FROM RestaurantUsers ru WHERE ru.restaurant.restaurantId = :restaurantId AND ru.role = :role")
    Optional<RestaurantUsers> findByRestaurantIdAndRole(@Param("restaurantId") Long restaurantId, @Param("role") Roles role);
}

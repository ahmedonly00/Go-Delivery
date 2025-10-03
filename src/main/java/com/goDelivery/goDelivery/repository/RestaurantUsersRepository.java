package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.RestaurantUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantUsersRepository extends JpaRepository<RestaurantUsers, Long> {
    boolean existsByEmail(String email);
    Optional<RestaurantUsers> findByEmail(String email);
    Optional<RestaurantUsers> findByFullNames(String fullNames);
    Optional<RestaurantUsers> findByUserId(Long userId);
    Optional<RestaurantUsers> findByVerificationToken(String token);
}

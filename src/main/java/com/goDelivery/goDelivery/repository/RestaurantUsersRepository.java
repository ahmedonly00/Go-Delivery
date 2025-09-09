package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.RestaurantUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantUsersRepository extends JpaRepository<RestaurantUsers, Long> {
    // Additional custom query methods can be added here if needed
    Optional<RestaurantUsers> findByEmail(String email);
}

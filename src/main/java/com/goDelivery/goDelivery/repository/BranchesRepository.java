package com.goDelivery.goDelivery.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goDelivery.goDelivery.model.Branches;

@Repository
public interface BranchesRepository extends JpaRepository<Branches, Long> {
    Optional<Branches> findByBranchId(Long branchId);
    Optional<Branches> findByBranchName(String branchName);
    Optional<Branches> findByRestaurant_RestaurantId(Long restaurantId);
    
}

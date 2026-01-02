package com.goDelivery.goDelivery.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goDelivery.goDelivery.model.Branches;
import com.goDelivery.goDelivery.Enum.ApprovalStatus;

@Repository
public interface BranchesRepository extends JpaRepository<Branches, Long> {
    Optional<Branches> findByBranchId(Long branchId);
    Optional<Branches> findByBranchName(String branchName);
    List<Branches> findByRestaurant_RestaurantId(Long restaurantId);
    List<Branches> findByRestaurant_RestaurantIdAndApprovalStatus(Long restaurantId, ApprovalStatus approvalStatus);
    boolean existsByRestaurant_RestaurantIdAndBranchName(Long restaurantId, String branchName);
    boolean existsByBranchIdAndRestaurant_RestaurantId(Long branchId, Long restaurantId);
    
}

package com.goDelivery.goDelivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goDelivery.goDelivery.model.BranchUsers;
import com.goDelivery.goDelivery.model.Branches;
import com.goDelivery.goDelivery.Enum.Roles;

@Repository
public interface BranchUsersRepository extends JpaRepository<BranchUsers, Long> {
    Optional<BranchUsers> findByEmail(String email);
    List<BranchUsers> findByBranch(Branches branch);
    List<BranchUsers> findByBranch_BranchId(Long branchId);
    List<BranchUsers> findByRestaurant_RestaurantIdAndRole(Long restaurantId, Roles role);
    Optional<BranchUsers> findByBranch_BranchIdAndEmail(Long branchId, String email);
    boolean existsByEmail(String email);
    boolean existsByBranch_BranchIdAndEmail(Long branchId, String email);
    List<BranchUsers> findByBranch_BranchIdAndIsActive(Long branchId, boolean isActive);
}

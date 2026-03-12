package com.goDelivery.goDelivery.modules.branch.repository;

import com.goDelivery.goDelivery.model.BranchMenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchMenuCategoryRepository extends JpaRepository<BranchMenuCategory, Long> {
    List<BranchMenuCategory> findByBranch_BranchId(Long branchId);
    long countByBranch_BranchId(Long branchId);
    boolean existsByBranch_BranchIdAndCategoryName(Long branchId, String categoryName);
    List<BranchMenuCategory> findByBranch_BranchIdAndCategoryNameContainingIgnoreCase(Long branchId, String categoryName);
}

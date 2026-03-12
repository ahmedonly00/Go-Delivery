package com.goDelivery.goDelivery.modules.branch.repository;

import com.goDelivery.goDelivery.modules.branch.model.BranchMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchMenuItemRepository extends JpaRepository<BranchMenuItem, Long> {
    List<BranchMenuItem> findByBranch_BranchId(Long branchId);
    List<BranchMenuItem> findByBranch_BranchIdAndIsAvailableTrue(Long branchId);
    List<BranchMenuItem> findByCategory_CategoryId(Long categoryId);
    List<BranchMenuItem> findByBranch_BranchIdAndCategory_CategoryId(Long branchId, Long categoryId);
}

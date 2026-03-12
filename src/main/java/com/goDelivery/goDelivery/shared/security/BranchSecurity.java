package com.goDelivery.goDelivery.shared.security;

import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.model.BranchUsers;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.BranchUsersRepository;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.repository.BranchesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component("branchSecurity")
@RequiredArgsConstructor
public class BranchSecurity {

    private final RestaurantUsersRepository restaurantUsersRepository;
    private final BranchUsersRepository branchUsersRepository;
    private final BranchesRepository branchesRepository;

    /**
     * Checks if the user is a RESTAURANT_ADMIN for the restaurant that owns the
     * branch.
     */
    @Transactional(readOnly = true)
    public boolean isRestaurantAdminOfBranch(String email, Long branchId) {
        log.debug("Checking if {} is RESTAURANT_ADMIN of branch {}", email, branchId);

        if (email == null || branchId == null) {
            return false;
        }

        // Find the user by email
        Optional<RestaurantUsers> userOpt = restaurantUsersRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }

        RestaurantUsers user = userOpt.get();

        // Must be a RESTAURANT_ADMIN
        if (user.getRole() != Roles.RESTAURANT_ADMIN) {
            return false;
        }

        // Get the branch to check its restaurant
        return branchesRepository.findById(branchId)
                .map(branch -> {
                    // Check if the user's restaurant owns this branch
                    boolean isOwner = user.getRestaurant() != null &&
                            user.getRestaurant().getRestaurantId().equals(branch.getRestaurant().getRestaurantId());
                    if (!isOwner) {
                        log.warn("User {} is a RESTAURANT_ADMIN but does not own branch {}", email, branchId);
                    }
                    return isOwner;
                })
                .orElse(false);
    }

    /**
     * Checks if the user is a BRANCH_MANAGER for the specific branch.
     */
    @Transactional(readOnly = true)
    public boolean isBranchManagerOfBranch(String email, Long branchId) {
        log.debug("Checking if {} is BRANCH_MANAGER of branch {}", email, branchId);

        if (email == null || branchId == null) {
            return false;
        }

        // Find the user by email in the branch users table
        Optional<BranchUsers> userOpt = branchUsersRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }

        BranchUsers user = userOpt.get();

        // Must be a BRANCH_MANAGER
        if (user.getRole() != Roles.BRANCH_MANAGER) {
            return false;
        }

        // The user must be assigned to the branch
        boolean isManager = user.getBranch() != null &&
                user.getBranch().getBranchId().equals(branchId);

        if (!isManager) {
            log.warn("User {} is a BRANCH_MANAGER but for a different branch (requested: {})", email, branchId);
        }

        return isManager;
    }
}

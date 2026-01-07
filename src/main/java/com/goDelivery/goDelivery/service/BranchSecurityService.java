package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.model.BranchUsers;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.model.Branches;
import com.goDelivery.goDelivery.repository.BranchUsersRepository;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.repository.BranchesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchSecurityService {

    private final RestaurantUsersRepository restaurantUsersRepository;
    private final BranchUsersRepository branchUsersRepository;
    private final BranchesRepository branchesRepository;

    public boolean canAccessBranch(Long branchId, String requiredRole) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        String username = auth.getName();
        log.debug("Checking access for user {} to branch {} with role {}", username, branchId, requiredRole);

        // Check if user is RESTAURANT_ADMIN
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RESTAURANT_ADMIN"))) {
            return isRestaurantAdminOfBranch(username, branchId);
        }

        // Check if user is BRANCH_MANAGER
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_BRANCH_MANAGER"))) {
            return isBranchManagerOfBranch(username, branchId);
        }

        return false;
    }

    public boolean isRestaurantAdminOfBranch(String username, Long branchId) {
        Optional<RestaurantUsers> adminOpt = restaurantUsersRepository.findByEmail(username);
        if (adminOpt.isEmpty()) {
            log.warn("Restaurant admin not found: {}", username);
            return false;
        }

        RestaurantUsers admin = adminOpt.get();
        Optional<Branches> branchOpt = branchesRepository.findById(branchId);
        if (branchOpt.isEmpty()) {
            log.warn("Branch not found: {}", branchId);
            return false;
        }

        Branches branch = branchOpt.get();
        boolean hasAccess = branch.getRestaurant().getRestaurantId().equals(admin.getRestaurant().getRestaurantId());
        
        if (hasAccess) {
            log.debug("Restaurant admin {} has access to branch {}", username, branchId);
        } else {
            log.warn("Restaurant admin {} does not have access to branch {}", username, branchId);
        }
        
        return hasAccess;
    }

    public boolean isBranchManagerOfBranch(String username, Long branchId) {
        Optional<BranchUsers> managerOpt = branchUsersRepository.findByEmail(username);
        if (managerOpt.isEmpty()) {
            log.warn("Branch manager not found: {}", username);
            return false;
        }

        BranchUsers manager = managerOpt.get();
        boolean hasAccess = manager.getBranch().getBranchId().equals(branchId) && manager.isActive();
        
        if (hasAccess) {
            log.debug("Branch manager {} has access to branch {}", username, branchId);
        } else {
            log.warn("Branch manager {} does not have access to branch {}", username, branchId);
        }
        
        return hasAccess;
    }

    public boolean canManageBranchUsers(Long branchId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // Only RESTAURANT_ADMIN can manage branch users
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RESTAURANT_ADMIN"))) {
            return isRestaurantAdminOfBranch(auth.getName(), branchId);
        }

        return false;
    }

    public RestaurantUsers getCurrentRestaurantUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new RuntimeException("No authenticated user found");
        }

        String username = auth.getName();
        return restaurantUsersRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Restaurant user not found: " + username));
    }

    public BranchUsers getCurrentBranchUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new RuntimeException("No authenticated user found");
        }

        String username = auth.getName();
        return branchUsersRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Branch user not found: " + username));
    }

    public boolean isBranchOfRestaurant(Long branchId, Long restaurantId) {
        Optional<Branches> branchOpt = branchesRepository.findById(branchId);
        if (branchOpt.isEmpty()) {
            return false;
        }

        Branches branch = branchOpt.get();
        return branch.getRestaurant().getRestaurantId().equals(restaurantId);
    }
}

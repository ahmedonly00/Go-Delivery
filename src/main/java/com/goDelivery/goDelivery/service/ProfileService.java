package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.profile.PasswordChangeRequest;
import com.goDelivery.goDelivery.dtos.profile.ProfileResponse;
import com.goDelivery.goDelivery.dtos.profile.ProfileUpdateRequest;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.ValidationException;
import com.goDelivery.goDelivery.model.BranchUsers;
import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.model.SuperAdmin;
import com.goDelivery.goDelivery.repository.BranchUsersRepository;
import com.goDelivery.goDelivery.repository.CustomerRepository;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.repository.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final SuperAdminRepository superAdminRepository;
    private final RestaurantUsersRepository restaurantUsersRepository;
    private final BranchUsersRepository branchUsersRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get the current authenticated user's profile
     */
    public ProfileResponse getMyProfile() {
        String email = getCurrentUserEmail();
        log.info("Fetching profile for user: {}", email);

        // Try to find user in each repository
        Optional<SuperAdmin> superAdmin = superAdminRepository.findByEmail(email);
        if (superAdmin.isPresent()) {
            return buildProfileResponse(superAdmin.get());
        }

        Optional<RestaurantUsers> restaurantUser = restaurantUsersRepository.findByEmail(email);
        if (restaurantUser.isPresent()) {
            return buildProfileResponse(restaurantUser.get());
        }

        Optional<BranchUsers> branchUser = branchUsersRepository.findByEmail(email);
        if (branchUser.isPresent()) {
            return buildProfileResponse(branchUser.get());
        }

        Optional<Customer> customer = customerRepository.findByEmail(email);
        if (customer.isPresent()) {
            return buildProfileResponse(customer.get());
        }

        throw new ResourceNotFoundException("User not found with email: " + email);
    }

    /**
     * Update the current authenticated user's profile
     */
    @Transactional
    public ProfileResponse updateMyProfile(ProfileUpdateRequest request) {
        String email = getCurrentUserEmail();
        log.info("Updating profile for user: {}", email);

        // Try to find and update user in each repository
        Optional<SuperAdmin> superAdmin = superAdminRepository.findByEmail(email);
        if (superAdmin.isPresent()) {
            return updateSuperAdminProfile(superAdmin.get(), request);
        }

        Optional<RestaurantUsers> restaurantUser = restaurantUsersRepository.findByEmail(email);
        if (restaurantUser.isPresent()) {
            return updateRestaurantUserProfile(restaurantUser.get(), request);
        }

        Optional<BranchUsers> branchUser = branchUsersRepository.findByEmail(email);
        if (branchUser.isPresent()) {
            return updateBranchUserProfile(branchUser.get(), request);
        }

        Optional<Customer> customer = customerRepository.findByEmail(email);
        if (customer.isPresent()) {
            return updateCustomerProfile(customer.get(), request);
        }

        throw new ResourceNotFoundException("User not found with email: " + email);
    }

    /**
     * Change the current authenticated user's password
     */
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        String email = getCurrentUserEmail();
        log.info("Changing password for user: {}", email);

        // Validate password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("New password and confirmation do not match");
        }

        // Try to find and update password in each repository
        Optional<SuperAdmin> superAdmin = superAdminRepository.findByEmail(email);
        if (superAdmin.isPresent()) {
            changeSuperAdminPassword(superAdmin.get(), request);
            return;
        }

        Optional<RestaurantUsers> restaurantUser = restaurantUsersRepository.findByEmail(email);
        if (restaurantUser.isPresent()) {
            changeRestaurantUserPassword(restaurantUser.get(), request);
            return;
        }

        Optional<BranchUsers> branchUser = branchUsersRepository.findByEmail(email);
        if (branchUser.isPresent()) {
            changeBranchUserPassword(branchUser.get(), request);
            return;
        }

        Optional<Customer> customer = customerRepository.findByEmail(email);
        if (customer.isPresent()) {
            changeCustomerPassword(customer.get(), request);
            return;
        }

        throw new ResourceNotFoundException("User not found with email: " + email);
    }

    // ==================== Helper Methods ====================

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ValidationException("User not authenticated");
        }
        return authentication.getName();
    }

    // ==================== Profile Response Builders ====================

    private ProfileResponse buildProfileResponse(SuperAdmin admin) {
        return ProfileResponse.builder()
                .userId(admin.getAdminId())
                .fullName(admin.getFullNames())
                .email(admin.getEmail())
                .phoneNumber(null) // SuperAdmin doesn't have phone number
                .role(admin.getRole())
                .userType("SUPER_ADMIN")
                .emailVerified(true)
                .isActive(admin.isActive())
                .createdAt(admin.getCreatedAt())
                .build();
    }

    private ProfileResponse buildProfileResponse(RestaurantUsers user) {
        return ProfileResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .userType("RESTAURANT_USER")
                .emailVerified(user.isEmailVerified())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .restaurantId(user.getRestaurant() != null ? user.getRestaurant().getRestaurantId() : null)
                .restaurantName(user.getRestaurant() != null ? user.getRestaurant().getRestaurantName() : null)
                .build();
    }

    private ProfileResponse buildProfileResponse(BranchUsers user) {
        return ProfileResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .userType("BRANCH_USER")
                .emailVerified(user.isEmailVerified())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .restaurantId(user.getRestaurant() != null ? user.getRestaurant().getRestaurantId() : null)
                .restaurantName(user.getRestaurant() != null ? user.getRestaurant().getRestaurantName() : null)
                .branchId(user.getBranch() != null ? user.getBranch().getBranchId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getBranchName() : null)
                .build();
    }

    private ProfileResponse buildProfileResponse(Customer customer) {
        return ProfileResponse.builder()
                .userId(customer.getCustomerId())
                .fullName(customer.getFullNames())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .role(customer.getRoles())
                .userType("CUSTOMER")
                .emailVerified(customer.isEmailVerified())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreatedAt())
                .address(customer.getLocation())
                .build();
    }

    // ==================== Profile Update Methods ====================

    private ProfileResponse updateSuperAdminProfile(SuperAdmin admin, ProfileUpdateRequest request) {
        boolean updated = false;

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            admin.setFullNames(request.getFullName().trim());
            updated = true;
        }

        // SuperAdmin doesn't have phoneNumber field, skip it
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            log.warn("SuperAdmin does not support phone number updates");
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(admin.getEmail())) {
                // Check if email already exists
                if (emailExistsInAnyRepository(request.getEmail())) {
                    throw new ValidationException("Email already exists");
                }
                admin.setEmail(request.getEmail().trim());
                updated = true;
            }
        }

        if (updated) {
            admin = superAdminRepository.save(admin);
            log.info("Updated super admin profile: {}", admin.getEmail());
        }

        return buildProfileResponse(admin);
    }

    private ProfileResponse updateRestaurantUserProfile(RestaurantUsers user, ProfileUpdateRequest request) {
        boolean updated = false;

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
            updated = true;
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
            updated = true;
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(user.getEmail())) {
                if (emailExistsInAnyRepository(request.getEmail())) {
                    throw new ValidationException("Email already exists");
                }
                user.setEmail(request.getEmail().trim());
                user.setEmailVerified(false); // Require re-verification
                updated = true;
            }
        }

        if (updated) {
            user = restaurantUsersRepository.save(user);
            log.info("Updated restaurant user profile: {}", user.getEmail());
        }

        return buildProfileResponse(user);
    }

    private ProfileResponse updateBranchUserProfile(BranchUsers user, ProfileUpdateRequest request) {
        boolean updated = false;

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
            updated = true;
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
            updated = true;
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(user.getEmail())) {
                if (emailExistsInAnyRepository(request.getEmail())) {
                    throw new ValidationException("Email already exists");
                }
                user.setEmail(request.getEmail().trim());
                user.setEmailVerified(false); // Require re-verification
                updated = true;
            }
        }

        if (updated) {
            user = branchUsersRepository.save(user);
            log.info("Updated branch user profile: {}", user.getEmail());
        }

        return buildProfileResponse(user);
    }

    private ProfileResponse updateCustomerProfile(Customer customer, ProfileUpdateRequest request) {
        boolean updated = false;

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            customer.setFullNames(request.getFullName().trim());
            updated = true;
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            customer.setPhoneNumber(request.getPhoneNumber().trim());
            updated = true;
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(customer.getEmail())) {
                if (emailExistsInAnyRepository(request.getEmail())) {
                    throw new ValidationException("Email already exists");
                }
                customer.setEmail(request.getEmail().trim());
                customer.setEmailVerified(false); // Require re-verification
                updated = true;
            }
        }

        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            customer.setLocation(request.getAddress().trim());
            updated = true;
        }

        if (updated) {
            customer = customerRepository.save(customer);
            log.info("Updated customer profile: {}", customer.getEmail());
        }

        return buildProfileResponse(customer);
    }

    // ==================== Password Change Methods ====================

    private void changeSuperAdminPassword(SuperAdmin admin, PasswordChangeRequest request) {
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        superAdminRepository.save(admin);
        log.info("Changed password for super admin: {}", admin.getEmail());
    }

    private void changeRestaurantUserPassword(RestaurantUsers user, PasswordChangeRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        restaurantUsersRepository.save(user);
        log.info("Changed password for restaurant user: {}", user.getEmail());
    }

    private void changeBranchUserPassword(BranchUsers user, PasswordChangeRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        branchUsersRepository.save(user);
        log.info("Changed password for branch user: {}", user.getEmail());
    }

    private void changeCustomerPassword(Customer customer, PasswordChangeRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), customer.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        customerRepository.save(customer);
        log.info("Changed password for customer: {}", customer.getEmail());
    }

    // ==================== Validation Methods ====================

    private boolean emailExistsInAnyRepository(String email) {
        return superAdminRepository.findByEmail(email).isPresent() ||
                restaurantUsersRepository.findByEmail(email).isPresent() ||
                branchUsersRepository.findByEmail(email).isPresent() ||
                customerRepository.findByEmail(email).isPresent();
    }
}

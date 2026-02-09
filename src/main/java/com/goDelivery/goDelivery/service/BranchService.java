package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dto.branch.BranchCreationDTO;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.Enum.ApprovalStatus;
import com.goDelivery.goDelivery.Enum.BranchSetupStatus;
import com.goDelivery.goDelivery.Enum.DeliveryType;
import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.UnauthorizedException;
import com.goDelivery.goDelivery.exception.ValidationException;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.repository.*;
import com.goDelivery.goDelivery.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchesRepository branchesRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final UsersService usersService;
    private final EmailService emailService;
    private final BranchUsersRepository branchUsersRepository;
    private final RestaurantUsersRepository restaurantUsersRepository;
    private final PasswordEncoder passwordEncoder;

    private final String UPLOAD_DIR = "uploads/branches/";

    // ==================== Basic CRUD Operations ====================

    @Transactional
    public BranchesDTO addBranchToRestaurant(Long restaurantId, BranchesDTO branchDTO) {
        // Get current user and verify they are a restaurant admin for this restaurant
        RestaurantUsers currentUser = usersService.getCurrentUser();

        // Verify the user belongs to the specified restaurant
        if (currentUser.getRestaurant() == null ||
                !currentUser.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new UnauthorizedException("You don't have permission to add branches to this restaurant");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        // Validate branch data
        validateBranchData(branchDTO);

        // Check if branch name already exists for this restaurant
        if (branchesRepository.existsByRestaurant_RestaurantIdAndBranchName(restaurantId, branchDTO.getBranchName())) {
            throw new ValidationException("A branch with this name already exists for this restaurant");
        }

        Branches branch = restaurantMapper.toBranch(branchDTO);
        branch.setRestaurant(restaurant);
        branch.setIsActive(true); // New branches are active by default

        Branches savedBranch = branchesRepository.save(branch);
        log.info("Created new branch '{}' for restaurant '{}' by user '{}'",
                branch.getBranchName(), restaurant.getRestaurantName(), currentUser.getEmail());

        return restaurantMapper.toBranchDTO(savedBranch);
    }

    @Transactional(readOnly = true)
    public BranchesDTO getBranchById(Long branchId) {
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        return restaurantMapper.toBranchDTO(branch);
    }

    @Transactional(readOnly = true)
    public List<BranchesDTO> getBranchesByRestaurant(Long restaurantId) {
        // Verify the authenticated user owns this restaurant
        verifyRestaurantAccess(restaurantId);

        List<Branches> branches = branchesRepository.findByRestaurant_RestaurantId(restaurantId);
        return branches.stream()
                .map(restaurantMapper::toBranchDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BranchesDTO updateBranch(Long branchId, BranchesDTO branchDTO) {
        Branches existingBranch = getBranchWithPermissionCheck(branchId);

        // Validate branch data
        validateBranchData(branchDTO);

        // Check if new branch name conflicts with existing branches (excluding this
        // one)
        if (!existingBranch.getBranchName().equals(branchDTO.getBranchName()) &&
                branchesRepository.existsByRestaurant_RestaurantIdAndBranchName(
                        existingBranch.getRestaurant().getRestaurantId(), branchDTO.getBranchName())) {
            throw new ValidationException("A branch with this name already exists for this restaurant");
        }

        // Update fields from DTO
        existingBranch.setBranchName(branchDTO.getBranchName());
        existingBranch.setAddress(branchDTO.getAddress());
        existingBranch.setPhoneNumber(branchDTO.getPhoneNumber());
        existingBranch.setIsActive(branchDTO.isActive());
        existingBranch.setUpdatedAt(LocalDate.now());

        Branches updatedBranch = branchesRepository.save(existingBranch);
        log.info("Updated branch '{}' by user '{}'", branchId, usersService.getCurrentUser().getEmail());

        return restaurantMapper.toBranchDTO(updatedBranch);
    }

    @Transactional
    public void deleteBranch(Long branchId) {
        Branches branch = getBranchWithPermissionCheck(branchId);

        // Check if branch has orders
        if (!branch.getOrders().isEmpty()) {
            throw new ValidationException("Cannot delete branch with existing orders. Deactivate it instead.");
        }

        branchesRepository.delete(branch);
        log.info("Deleted branch '{}' by user '{}'", branchId, usersService.getCurrentUser().getEmail());
    }

    // ==================== Comprehensive Branch Creation ====================

    @Transactional
    public BranchesDTO createBranch(Long restaurantId, BranchCreationDTO creationDTO) {
        log.info("Creating new branch '{}' for restaurant {}", creationDTO.getBranchName(), restaurantId);

        // Verify restaurant admin
        verifyRestaurantAdmin(restaurantId);

        // Check if branch already exists
        if (branchesRepository.existsByRestaurant_RestaurantIdAndBranchName(
                restaurantId, creationDTO.getBranchName())) {
            throw new ValidationException("A branch with this name already exists");
        }

        // Get restaurant
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        // Check if restaurant is approved, if so, auto-approve the branch
        boolean autoApprove = restaurant.getApprovalStatus() == ApprovalStatus.APPROVED;

        // Create basic branch with essential fields only
        // Delivery settings and other details configured by Branch Manager during setup
        Branches branch = Branches.builder()
                .branchName(creationDTO.getBranchName())
                .address(formatAddress(creationDTO))
                .phoneNumber(creationDTO.getPhoneNumber())
                .email(creationDTO.getEmail())
                .description(creationDTO.getDescription())
                .deliveryType(DeliveryType.SYSTEM_DELIVERY)
                .deliveryAvailable(true)
                .approvalStatus(autoApprove ? ApprovalStatus.APPROVED : ApprovalStatus.PENDING)
                .setupStatus(BranchSetupStatus.ACCOUNT_CREATED)
                .isActive(true)
                .restaurant(restaurant)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        // Set branch active status based on approval
        branch.setIsActive(autoApprove);

        // Set approval details if auto-approved
        if (autoApprove) {
            branch.setApprovedBy("System (Restaurant Approved)");
            branch.setApprovedAt(LocalDate.now());
        }

        // Save branch
        Branches savedBranch = branchesRepository.save(branch);

        // Create branch manager
        createBranchManager(savedBranch, creationDTO, autoApprove);

        log.info("Branch created successfully: {} (ID: {}) - Status: {}",
                savedBranch.getBranchName(), savedBranch.getBranchId(),
                savedBranch.getApprovalStatus());

        return restaurantMapper.toBranchDTO(savedBranch);
    }

    @Transactional
    public BranchesDTO updateBranchComprehensive(Long branchId, BranchCreationDTO updateDTO,
            MultipartFile logoFile) {
        log.info("Updating branch {}", branchId);

        // Verify access
        verifyBranchAccess(branchId);

        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        // Update branch details
        updateBranchFromDTO(branch, updateDTO);

        // Update logo if provided
        if (logoFile != null && !logoFile.isEmpty()) {
            String logoUrl = uploadFile(logoFile, "logo");
            branch.setLogoUrl(logoUrl);
        }

        Branches updatedBranch = branchesRepository.save(branch);
        log.info("Branch updated successfully: {}", branchId);

        return restaurantMapper.toBranchDTO(updatedBranch);
    }

    // ==================== Branch Status Management ====================

    @Transactional
    public BranchesDTO activateBranch(Long branchId) {
        verifyBranchAccess(branchId);

        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));

        branch.setIsActive(true);
        Branches savedBranch = branchesRepository.save(branch);

        log.info("Branch activated: {}", branchId);
        return restaurantMapper.toBranchDTO(savedBranch);
    }

    @Transactional
    public BranchesDTO deactivateBranch(Long branchId) {
        verifyBranchAccess(branchId);

        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));

        branch.setIsActive(false);
        Branches savedBranch = branchesRepository.save(branch);

        log.info("Branch deactivated: {}", branchId);
        return restaurantMapper.toBranchDTO(savedBranch);
    }

    @Transactional
    public BranchesDTO toggleBranchStatus(Long branchId, boolean isActive) {
        Branches branch = getBranchWithPermissionCheck(branchId);
        branch.setIsActive(isActive);
        Branches updatedBranch = branchesRepository.save(branch);
        log.info("{} branch {}", isActive ? "Activated" : "Deactivated", branchId);
        return restaurantMapper.toBranchDTO(updatedBranch);
    }

    // ==================== User-specific Operations ====================

    @Transactional(readOnly = true)
    public List<BranchesDTO> getCurrentUserRestaurantBranches() {
        RestaurantUsers currentUser = usersService.getCurrentUser();

        if (currentUser.getRestaurant() == null) {
            throw new UnauthorizedException("User is not associated with any restaurant");
        }

        return branchesRepository.findByRestaurant_RestaurantId(currentUser.getRestaurant().getRestaurantId())
                .stream()
                .map(restaurantMapper::toBranchDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BranchesDTO getBranchForCurrentUser(Long branchId) {
        RestaurantUsers currentUser = usersService.getCurrentUser();

        if (currentUser.getRestaurant() == null) {
            throw new UnauthorizedException("User is not associated with any restaurant");
        }

        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));

        // Verify branch belongs to user's restaurant
        if (!branch.getRestaurant().getRestaurantId().equals(currentUser.getRestaurant().getRestaurantId())) {
            throw new UnauthorizedException("You don't have permission to view this branch");
        }

        return restaurantMapper.toBranchDTO(branch);
    }

    // ==================== Utility Methods ====================

    @Transactional(readOnly = true)
    public boolean isBranchBelongsToRestaurant(Long branchId, Long restaurantId) {
        return branchesRepository.existsByBranchIdAndRestaurant_RestaurantId(branchId, restaurantId);
    }

    @Transactional(readOnly = true)
    public Long getRestaurantIdByBranch(Long branchId) {
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        return branch.getRestaurant().getRestaurantId();
    }

    public List<BranchesDTO> getAllBranches() {
        List<Branches> branches = branchesRepository.findAll();
        return branches.stream()
                .map(restaurantMapper::toBranchDTO)
                .collect(Collectors.toList());
    }

    // ==================== Private Helper Methods ====================

    private void updateBranchFromDTO(Branches branch, BranchCreationDTO dto) {
        branch.setBranchName(dto.getBranchName());
        branch.setAddress(formatAddress(dto));
        branch.setPhoneNumber(dto.getPhoneNumber());
        branch.setEmail(dto.getEmail());
        branch.setDescription(dto.getDescription());
        branch.setUpdatedAt(LocalDate.now());
    }

    private String formatAddress(BranchCreationDTO dto) {
        StringBuilder address = new StringBuilder();
        if (dto.getAddress() != null) {
            address.append(dto.getAddress());
        }
        address.append(", ").append(dto.getCity());
        address.append(", ").append(dto.getState());
        return address.toString();
    }

    private String uploadFile(MultipartFile file, String prefix) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR + prefix + "/" + fileName);

            // Create directory if it doesn't exist
            Files.createDirectories(uploadPath.getParent());

            // Save file
            Files.copy(file.getInputStream(), uploadPath);

            return "/" + uploadPath.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    private void verifyRestaurantAccess(Long restaurantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        String username = authentication.getName();
        RestaurantUsers restaurantUser = restaurantUsersRepository.findByEmail(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!restaurantUser.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new UnauthorizedException("You do not have permission to access this restaurant");
        }
    }

    private void verifyBranchAccess(Long branchId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        String username = authentication.getName();

        // Check if restaurant admin
        RestaurantUsers restaurantUser = restaurantUsersRepository.findByEmail(username).orElse(null);
        if (restaurantUser != null) {
            Branches branch = branchesRepository.findById(branchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));

            if (restaurantUser.getRestaurant().getRestaurantId().equals(branch.getRestaurant().getRestaurantId())) {
                return; // Restaurant admin has access
            }
        }

        throw new UnauthorizedException("You do not have permission to access this branch");
    }

    private void verifyRestaurantAdmin(Long restaurantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        String username = authentication.getName();
        RestaurantUsers admin = restaurantUsersRepository.findByEmail(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!admin.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new UnauthorizedException("You are not an admin for this restaurant");
        }
    }

    private Branches getBranchWithPermissionCheck(Long branchId) {
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));

        // Verify user has access to the restaurant this branch belongs to
        verifyRestaurantAccess(branch.getRestaurant().getRestaurantId());

        return branch;
    }

    private void validateBranchData(BranchesDTO branchDTO) {
        if (StringUtils.isBlank(branchDTO.getBranchName())) {
            throw new ValidationException("Branch name is required");
        }
        if (StringUtils.isBlank(branchDTO.getAddress())) {
            throw new ValidationException("Branch address is required");
        }
        if (StringUtils.isBlank(branchDTO.getPhoneNumber())) {
            throw new ValidationException("Phone number is required");
        }
        if (!com.goDelivery.goDelivery.util.PhoneNumberValidator.isValid(branchDTO.getPhoneNumber())) {
            throw new ValidationException(com.goDelivery.goDelivery.util.PhoneNumberValidator.getErrorMessage());
        }
    }

    private void createBranchManager(Branches branch, BranchCreationDTO creationDTO, boolean isAutoApproved) {
        // Create branch manager account
        BranchUsers manager = new BranchUsers();
        manager.setFullName(creationDTO.getManagerName());
        manager.setEmail(creationDTO.getManagerEmail());
        manager.setPhoneNumber(creationDTO.getManagerPhone());

        // Hash the password
        String hashedPassword = passwordEncoder.encode(creationDTO.getManagerPassword());
        manager.setPassword(hashedPassword);
        manager.setRole(Roles.BRANCH_MANAGER);
        manager.setPermissions("FULL_ACCESS");
        manager.setBranch(branch);
        manager.setRestaurant(branch.getRestaurant());
        manager.setSetupComplete(false);
        manager.setCreatedAt(LocalDate.now());
        manager.setUpdatedAt(LocalDate.now());

        branchUsersRepository.save(manager);

        // Send credentials to branch manager via email
        emailService.sendBranchManagerCredentials(
                creationDTO.getManagerEmail(),
                creationDTO.getManagerName(),
                branch.getBranchName(),
                branch.getRestaurant().getRestaurantName(),
                creationDTO.getManagerPassword(),
                isAutoApproved);

        log.info("Branch manager created: {} - Branch status: {}",
                creationDTO.getManagerEmail(),
                isAutoApproved ? "AUTO-APPROVED" : "PENDING APPROVAL");
    }
}

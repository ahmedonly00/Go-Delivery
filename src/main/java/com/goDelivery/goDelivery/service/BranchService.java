package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dto.branch.BranchCreationDTO;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.Enum.ApprovalStatus;
import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.UnauthorizedException;
import com.goDelivery.goDelivery.exception.ValidationException;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.repository.*;
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
import java.time.LocalTime;
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
    private final UsersRepository usersRepository;
    private final UsersService usersService;
    private final MenuCategoryRepository menuCategoryRepository;
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
        branch.setActive(true); // New branches are active by default
        
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
        
        // Check if new branch name conflicts with existing branches (excluding this one)
        if (!existingBranch.getBranchName().equals(branchDTO.getBranchName()) &&
            branchesRepository.existsByRestaurant_RestaurantIdAndBranchName(
                existingBranch.getRestaurant().getRestaurantId(), branchDTO.getBranchName())) {
            throw new ValidationException("A branch with this name already exists for this restaurant");
        }

        // Update fields from DTO
        existingBranch.setBranchName(branchDTO.getBranchName());
        existingBranch.setAddress(branchDTO.getAddress());
        existingBranch.setLatitude(branchDTO.getLatitude());
        existingBranch.setLongitude(branchDTO.getLongitude());
        existingBranch.setPhoneNumber(branchDTO.getPhoneNumber());
        existingBranch.setOperatingHours(branchDTO.getOperatingHours());
        existingBranch.setActive(branchDTO.isActive());
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
    public BranchesDTO createBranch(Long restaurantId, BranchCreationDTO creationDTO,
                                   MultipartFile logoFile,
                                   MultipartFile[] documentFiles) {
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
        
        // Create branch
        Branches branch = buildBranchFromDTO(creationDTO, restaurant);
        
        // Upload logo if provided
        if (logoFile != null && !logoFile.isEmpty()) {
            String logoUrl = uploadFile(logoFile, "logo");
            branch.setLogoUrl(logoUrl);
        }
        
        // Upload documents if provided
        if (documentFiles != null && documentFiles.length > 0) {
            uploadDocuments(branch, documentFiles);
        }
        
        // Save branch
        Branches savedBranch = branchesRepository.save(branch);
        
        // Create branch manager
        createBranchManager(savedBranch, creationDTO);
        
        // Create initial menu categories
        if (creationDTO.getInitialMenuCategories() != null && 
            !creationDTO.getInitialMenuCategories().isEmpty()) {
            createInitialMenuCategories(savedBranch, creationDTO.getInitialMenuCategories());
        }
        
        log.info("Branch created successfully: {} (ID: {})", savedBranch.getBranchName(), savedBranch.getBranchId());
        
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
        
        branch.setActive(true);
        Branches savedBranch = branchesRepository.save(branch);
        
        log.info("Branch activated: {}", branchId);
        return restaurantMapper.toBranchDTO(savedBranch);
    }

    @Transactional
    public BranchesDTO deactivateBranch(Long branchId) {
        verifyBranchAccess(branchId);
        
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        
        branch.setActive(false);
        Branches savedBranch = branchesRepository.save(branch);
        
        log.info("Branch deactivated: {}", branchId);
        return restaurantMapper.toBranchDTO(savedBranch);
    }

    @Transactional
    public BranchesDTO toggleBranchStatus(Long branchId, boolean isActive) {
        Branches branch = getBranchWithPermissionCheck(branchId);
        branch.setActive(isActive);
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

    private Branches buildBranchFromDTO(BranchCreationDTO dto, Restaurant restaurant) {
        Branches branch = Branches.builder()
                .branchName(dto.getBranchName())
                .address(formatAddress(dto))
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .website(dto.getWebsite())
                .description(dto.getDescription())
                .operatingHours(formatOperatingHours(dto))
                .deliveryAvailable(dto.getDeliveryAvailable())
                .deliveryRadius(dto.getDeliveryRadius())
                .minimumOrderAmount(dto.getMinimumOrderAmount())
                .deliveryFee(dto.getDeliveryFee())
                .approvalStatus(ApprovalStatus.PENDING)
                .restaurant(restaurant)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
        
        branch.setActive(false); // Inactive until approved
        return branch;
    }
    
    private void updateBranchFromDTO(Branches branch, BranchCreationDTO dto) {
        branch.setBranchName(dto.getBranchName());
        branch.setAddress(formatAddress(dto));
        branch.setLatitude(dto.getLatitude());
        branch.setLongitude(dto.getLongitude());
        branch.setPhoneNumber(dto.getPhoneNumber());
        branch.setEmail(dto.getEmail());
        branch.setWebsite(dto.getWebsite());
        branch.setDescription(dto.getDescription());
        branch.setOperatingHours(formatOperatingHours(dto));
        branch.setDeliveryAvailable(dto.getDeliveryAvailable());
        branch.setDeliveryRadius(dto.getDeliveryRadius());
        branch.setMinimumOrderAmount(dto.getMinimumOrderAmount());
        branch.setDeliveryFee(dto.getDeliveryFee());
        branch.setUpdatedAt(LocalDate.now());
    }
    
    private String formatAddress(BranchCreationDTO dto) {
        StringBuilder address = new StringBuilder();
        address.append(dto.getAddress());
        if (dto.getAddressLine2() != null && !dto.getAddressLine2().isEmpty()) {
            address.append(", ").append(dto.getAddressLine2());
        }
        address.append(", ").append(dto.getCity());
        address.append(", ").append(dto.getState());
        address.append(", ").append(dto.getPostalCode());
        address.append(", ").append(dto.getCountry());
        return address.toString();
    }
    
    private String formatOperatingHours(BranchCreationDTO dto) {
        StringBuilder hours = new StringBuilder();
        hours.append("Mon: ").append(formatTime(dto.getMondayOpen())).append(" - ")
             .append(formatTime(dto.getMondayClose())).append("\n");
        hours.append("Tue: ").append(formatTime(dto.getTuesdayOpen())).append(" - ")
             .append(formatTime(dto.getTuesdayClose())).append("\n");
        hours.append("Wed: ").append(formatTime(dto.getWednesdayOpen())).append(" - ")
             .append(formatTime(dto.getWednesdayClose())).append("\n");
        hours.append("Thu: ").append(formatTime(dto.getThursdayOpen())).append(" - ")
             .append(formatTime(dto.getThursdayClose())).append("\n");
        hours.append("Fri: ").append(formatTime(dto.getFridayOpen())).append(" - ")
             .append(formatTime(dto.getFridayClose())).append("\n");
        hours.append("Sat: ").append(formatTime(dto.getSaturdayOpen())).append(" - ")
             .append(formatTime(dto.getSaturdayClose())).append("\n");
        hours.append("Sun: ").append(formatTime(dto.getSundayOpen())).append(" - ")
             .append(formatTime(dto.getSundayClose()));
        return hours.toString();
    }
    
    private String formatTime(LocalTime time) {
        return time != null ? time.toString() : "Closed";
    }
    
    private void createBranchManager(Branches branch, BranchCreationDTO dto) {
        // Hash password first
        String hashedPassword = passwordEncoder.encode(dto.getManagerPassword());
        
        // Create manager manually
        BranchUsers manager = new BranchUsers();
        manager.setFullName(dto.getManagerName());
        manager.setEmail(dto.getManagerEmail());
        manager.setPhoneNumber(dto.getManagerPhone());
        manager.setPassword(hashedPassword);
        manager.setRole(Roles.BRANCH_MANAGER);
        manager.setPermissions("FULL_ACCESS");
        manager.setBranch(branch);
        manager.setRestaurant(branch.getRestaurant());
        manager.setSetupComplete(false);
        manager.setCreatedAt(LocalDate.now());
        manager.setUpdatedAt(LocalDate.now());
        
        branchUsersRepository.save(manager);
        
        log.info("Branch manager created: {}", dto.getManagerEmail());
    }
    
    private void createInitialMenuCategories(Branches branch, List<String> categoryNames) {
        for (String categoryName : categoryNames) {
            // Create menu category
            MenuCategory category = new MenuCategory();
            category.setCategoryName(categoryName);
            category.setBranch(branch);
            
            menuCategoryRepository.save(category);
            log.info("Created initial menu category: {} for branch: {}", categoryName, branch.getBranchId());
        }
    }
    
    private void uploadDocuments(Branches branch, MultipartFile[] documents) {
        for (int i = 0; i < documents.length; i++) {
            if (documents[i] != null && !documents[i].isEmpty()) {
                String docUrl = uploadFile(documents[i], "document_" + i);
                if (i == 0) {
                    branch.setBusinessDocumentUrl(docUrl);
                } else if (i == 1) {
                    branch.setOperatingLicenseUrl(docUrl);
                }
            }
        }
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
        
        // TODO: Add branch user verification if needed
        
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
        if (branchDTO.getLatitude() == null || branchDTO.getLongitude() == null) {
            throw new ValidationException("Branch coordinates (latitude and longitude) are required");
        }
        if (StringUtils.isBlank(branchDTO.getOperatingHours())) {
            throw new ValidationException("Operating hours are required");
        }
        // Validate phone number format (basic validation)
        if (!branchDTO.getPhoneNumber().matches("^[+]?[0-9]{10,15}$")) {
            throw new ValidationException("Invalid phone number format");
        }
    }
}

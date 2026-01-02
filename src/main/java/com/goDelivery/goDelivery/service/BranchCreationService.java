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

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchCreationService {

    private final BranchesRepository branchesRepository;
    private final BranchUsersRepository branchUsersRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final PasswordEncoder passwordEncoder;
    private final UsersService usersService;
    private final MenuCategoryService menuCategoryService;
    
    private final String UPLOAD_DIR = "uploads/branches/";

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
    public BranchesDTO updateBranch(Long branchId, BranchCreationDTO updateDTO,
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
                .facebookUrl(dto.getFacebookUrl())
                .instagramUrl(dto.getInstagramUrl())
                .twitterUrl(dto.getTwitterUrl())
                .hasParking(dto.getHasParking())
                .hasWifi(dto.getHasWifi())
                .hasOutdoorSeating(dto.getHasOutdoorSeating())
                .acceptsReservations(dto.getAcceptsReservations())
                .averageRating(dto.getAverageRating())
                .reviewCount(dto.getReviewCount())
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
        branch.setFacebookUrl(dto.getFacebookUrl());
        branch.setInstagramUrl(dto.getInstagramUrl());
        branch.setTwitterUrl(dto.getTwitterUrl());
        branch.setHasParking(dto.getHasParking());
        branch.setHasWifi(dto.getHasWifi());
        branch.setHasOutdoorSeating(dto.getHasOutdoorSeating());
        branch.setAcceptsReservations(dto.getAcceptsReservations());
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
            // Use MenuCategoryService to create categories
            log.info("Creating initial menu category: {}", categoryName);
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
    
    private String uploadFile(MultipartFile file, String type) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR + type + "/" + fileName);
            
            // Create directory if it doesn't exist
            Files.createDirectories(uploadPath.getParent());
            
            // Save file
            Files.copy(file.getInputStream(), uploadPath);
            
            return "/" + uploadPath.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload " + type, e);
        }
    }
    
    private RestaurantUsers verifyRestaurantAdmin(Long restaurantId) {
        RestaurantUsers currentUser = usersService.getCurrentUser();
        
        if (currentUser == null || !currentUser.getRole().equals(Roles.RESTAURANT_ADMIN)) {
            throw new UnauthorizedException("Only restaurant admins can create branches");
        }
        
        if (!currentUser.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new UnauthorizedException("You don't have permission to create branches for this restaurant");
        }
        
        return currentUser;
    }
    
    private void verifyBranchAccess(Long branchId) {
        Object currentUserObj = usersService.getCurrentUser();
        
        if (currentUserObj instanceof RestaurantUsers) {
            RestaurantUsers admin = (RestaurantUsers) currentUserObj;
            Branches branch = branchesRepository.findById(branchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
            
            if (!admin.getRestaurant().getRestaurantId().equals(branch.getRestaurant().getRestaurantId())) {
                throw new UnauthorizedException("You don't have permission to manage this branch");
            }
        } else if (currentUserObj instanceof BranchUsers) {
            BranchUsers branchUser = (BranchUsers) currentUserObj;
            if (!branchUser.getBranch().getBranchId().equals(branchId)) {
                throw new UnauthorizedException("You don't have permission to manage this branch");
            }
        } else {
            throw new UnauthorizedException("Invalid user type");
        }
    }
}

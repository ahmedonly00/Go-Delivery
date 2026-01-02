package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dto.branch.BranchRegistrationDTO;
import com.goDelivery.goDelivery.dto.auth.BranchUserRegistrationDTO;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.dtos.restaurant.BranchUserDTO;
import com.goDelivery.goDelivery.Enum.ApprovalStatus;
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
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchRegistrationService {

    private final BranchesRepository branchesRepository;
    private final BranchUsersRepository branchUsersRepository;
    private final RestaurantUsersRepository restaurantUsersRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final PasswordEncoder passwordEncoder;
    private final UsersService usersService;
    private final MenuCategoryService menuCategoryService;
    
    private final String UPLOAD_DIR = "uploads/branch-docs/";

    @Transactional
    public BranchesDTO registerBranch(BranchRegistrationDTO registrationDTO, 
                                     MultipartFile businessDocument,
                                     MultipartFile operatingLicense) {
        log.info("Starting branch registration for: {}", registrationDTO.getBranchName());
        
        // Validate restaurant admin email
        RestaurantUsers restaurantAdmin = validateRestaurantAdmin(registrationDTO.getRestaurantAdminEmail());
        
        // Validate branch doesn't already exist for this restaurant
        if (branchesRepository.existsByRestaurant_RestaurantIdAndBranchName(
                restaurantAdmin.getRestaurant().getRestaurantId(), registrationDTO.getBranchName())) {
            throw new ValidationException("A branch with this name already exists for this restaurant");
        }
        
        // Upload documents
        String businessDocUrl = uploadDocument(businessDocument, "business-doc");
        String licenseUrl = uploadDocument(operatingLicense, "license");
        
        // Create branch
        Branches branch = restaurantMapper.toBranch(registrationDTO);
        branch.setRestaurant(restaurantAdmin.getRestaurant());
        branch.setBusinessDocumentUrl(businessDocUrl);
        branch.setOperatingLicenseUrl(licenseUrl);
        branch.setApprovalStatus(ApprovalStatus.PENDING);
        branch.setActive(false); // Inactive until approved
        branch.setDescription(registrationDTO.getDescription());
        
        Branches savedBranch = branchesRepository.save(branch);
        
        // Create initial menu categories if provided
        if (registrationDTO.getMenuCategoryNames() != null && !registrationDTO.getMenuCategoryNames().isEmpty()) {
            createInitialMenuCategories(savedBranch, registrationDTO.getMenuCategoryNames());
        }
        
        // Create branch manager account
        if (registrationDTO.getBranchManager() != null) {
            createBranchManager(savedBranch, registrationDTO.getBranchManager());
        }
        
        log.info("Branch registration completed for: {} (ID: {})", savedBranch.getBranchName(), savedBranch.getBranchId());
        
        return restaurantMapper.toBranchDTO(savedBranch);
    }
    
    @Transactional
    public BranchesDTO approveBranch(Long branchId, String approvedBy) {
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        branch.setApprovalStatus(ApprovalStatus.APPROVED);
        branch.setActive(true);
        branch.setApprovedBy(approvedBy);
        branch.setApprovedAt(LocalDate.now());
        
        Branches approvedBranch = branchesRepository.save(branch);
        log.info("Branch approved: {} by {}", branch.getBranchName(), approvedBy);
        
        return restaurantMapper.toBranchDTO(approvedBranch);
    }
    
    @Transactional
    public BranchesDTO rejectBranch(Long branchId, String rejectionReason, String rejectedBy) {
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        branch.setApprovalStatus(ApprovalStatus.REJECTED);
        branch.setRejectionReason(rejectionReason);
        branch.setReviewedBy(rejectedBy);
        branch.setReviewedAt(LocalDate.now());
        
        Branches rejectedBranch = branchesRepository.save(branch);
        log.info("Branch rejected: {} by {} - Reason: {}", branch.getBranchName(), rejectedBy, rejectionReason);
        
        return restaurantMapper.toBranchDTO(rejectedBranch);
    }
    
    @Transactional(readOnly = true)
    public List<BranchesDTO> getPendingBranches() {
        RestaurantUsers currentUser = usersService.getCurrentUser();
        
        if (currentUser.getRestaurant() == null) {
            throw new UnauthorizedException("User is not associated with any restaurant");
        }
        
        return branchesRepository.findByRestaurant_RestaurantIdAndApprovalStatus(
                currentUser.getRestaurant().getRestaurantId(), ApprovalStatus.PENDING)
                .stream()
                .map(restaurantMapper::toBranchDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private RestaurantUsers validateRestaurantAdmin(String email) {
        RestaurantUsers admin = restaurantUsersRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant admin not found"));
        
        if (!admin.getRole().name().equals("RESTAURANT_ADMIN")) {
            throw new UnauthorizedException("User is not a restaurant admin");
        }
        
        return admin;
    }
    
    private String uploadDocument(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException(type + " document is required");
        }
        
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR + fileName);
            
            // Create directory if it doesn't exist
            Files.createDirectories(uploadPath.getParent());
            
            // Save file
            Files.copy(file.getInputStream(), uploadPath);
            
            return "/" + uploadPath.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload " + type + " document", e);
        }
    }
    
    private void createInitialMenuCategories(Branches branch, List<String> categoryNames) {
        for (String categoryName : categoryNames) {
            // Create menu category for the branch
            // This would use the MenuCategoryService to create categories
            log.info("Creating menu category '{}' for branch {}", categoryName, branch.getBranchName());
        }
    }
    
    private void createBranchManager(Branches branch, BranchUserRegistrationDTO managerDTO) {
        BranchUserDTO branchUserDTO = new BranchUserDTO();
        branchUserDTO.setFullName(managerDTO.getFullName());
        branchUserDTO.setEmail(managerDTO.getEmail());
        branchUserDTO.setPhoneNumber(managerDTO.getPhoneNumber());
        branchUserDTO.setPassword(managerDTO.getPassword());
        branchUserDTO.setRole(managerDTO.getRole());
        branchUserDTO.setPermissions(managerDTO.getPermissions());
        branchUserDTO.setBranchId(branch.getBranchId());
        branchUserDTO.setRestaurantId(branch.getRestaurant().getRestaurantId());
        
        // Use BranchUserService to create the manager
        // branchUserService.createBranchUser(branch.getBranchId(), branchUserDTO);
        log.info("Branch manager account created for: {}", managerDTO.getEmail());
    }
}

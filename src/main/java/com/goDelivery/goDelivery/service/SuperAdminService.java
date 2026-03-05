package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.ApprovalStatus;
import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.Enum.StatsPeriod;
import com.goDelivery.goDelivery.dtos.admin.CreateSuperAdminRequest;
import com.goDelivery.goDelivery.dtos.admin.SystemStatsDTO;
import com.goDelivery.goDelivery.dtos.biker.BikerDetailsResponse;
import com.goDelivery.goDelivery.dtos.biker.BikerUpdateRequest;
import com.goDelivery.goDelivery.exception.DuplicateResourceException;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.BikerMapper;
import com.goDelivery.goDelivery.model.Bikers;
import com.goDelivery.goDelivery.model.SuperAdmin;
import com.goDelivery.goDelivery.repository.BikersRepository;
import com.goDelivery.goDelivery.repository.BranchUsersRepository;
import com.goDelivery.goDelivery.repository.OrderRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.repository.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuperAdminService {

    private final SuperAdminRepository superAdminRepository;
    private final PasswordEncoder passwordEncoder;
    private final BikersRepository bikersRepository;
    private final BikerMapper bikerMapper;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantUsersRepository restaurantUsersRepository;
    private final BranchUsersRepository branchUsersRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public SuperAdmin createSuperAdmin(CreateSuperAdminRequest request) {
        // Check if email is already in use
        if (superAdminRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use");
        }

        // Create new super admin
        SuperAdmin superAdmin = SuperAdmin.builder()
                .fullNames(request.getFullNames())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Roles.SUPER_ADMIN)
                .isActive(true)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        // Save and return the new super admin
        return superAdminRepository.save(superAdmin);
    }

    public List<SuperAdmin> getAllSuperAdmins() {
        return superAdminRepository.findAll();
    }
    
    //Get all bikers
    public List<BikerDetailsResponse> getAllBikers() {
        return bikersRepository.findAll().stream()
                .map(bikerMapper::toBikerDetailsResponse)
                .collect(Collectors.toList());
    }
    
    
    //Get active bikers only
    public List<BikerDetailsResponse> getActiveBikers() {
        return bikersRepository.findByIsActiveTrue().stream()
                .map(bikerMapper::toBikerDetailsResponse)
                .collect(Collectors.toList());
    }
    
    
    //Get biker by ID
    public BikerDetailsResponse getBikerById(Long bikerId) {
        Bikers biker = bikersRepository.findById(bikerId)
                .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + bikerId));
        return bikerMapper.toBikerDetailsResponse(biker);
    }
    
    
     //Update biker details
    @Transactional
    public BikerDetailsResponse updateBiker(Long bikerId, BikerUpdateRequest updateRequest) {
        Bikers biker = bikersRepository.findById(bikerId)
                .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + bikerId));
        
        // Update biker details
        biker.setFullNames(updateRequest.getFullNames());
        biker.setEmail(updateRequest.getEmail());
        biker.setPhoneNumber(updateRequest.getPhoneNumber());
        biker.setNationalId(updateRequest.getNationalId());
        biker.setLicenseNumber(updateRequest.getLicenseNumber());
        biker.setVehicleType(updateRequest.getVehicleType());
        biker.setVehiclePlate(updateRequest.getVehiclePlate());
        biker.setVehicleModel(updateRequest.getVehicleModel());
        biker.setProfileImage(updateRequest.getProfileImage());
        biker.setActive(updateRequest.isActive());
        biker.setUpdatedAt(LocalDate.now());
        
        Bikers updatedBiker = bikersRepository.save(biker);
        return bikerMapper.toBikerDetailsResponse(updatedBiker);
    }
    
     //Delete a biker (soft delete)
    @Transactional
    public void deleteBiker(Long bikerId) {
        Bikers biker = bikersRepository.findById(bikerId)
                .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + bikerId));
        
        // Soft delete by setting isActive to false
        biker.setActive(false);
        biker.setUpdatedAt(LocalDate.now());
        bikersRepository.save(biker);
    }
    
     //Activate/Deactivate a biker account
    @Transactional
    public void activateBiker(Long bikerId, boolean isActive) {
        Bikers biker = bikersRepository.findById(bikerId)
                .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + bikerId));
        
        biker.setActive(isActive);
        biker.setUpdatedAt(LocalDate.now());
        bikersRepository.save(biker);
        
        log.info("Biker {} {}", bikerId, isActive ? "activated" : "deactivated");
    }

    public SuperAdmin getSuperAdminById(Long id) {
        return superAdminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SuperAdmin not found with id: " + id));
    }

    public SuperAdmin updateSuperAdmin(Long id, SuperAdmin superAdmin) {
        SuperAdmin existingSuperAdmin = getSuperAdminById(id);
        existingSuperAdmin.setFullNames(superAdmin.getFullNames());
        existingSuperAdmin.setEmail(superAdmin.getEmail());
        existingSuperAdmin.setRole(superAdmin.getRole());
        existingSuperAdmin.setActive(true);
        existingSuperAdmin.setUpdatedAt(LocalDate.now());
        return superAdminRepository.save(existingSuperAdmin);
    }

    public void deleteSuperAdmin(Long id) {
        superAdminRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public SystemStatsDTO getSystemStats(StatsPeriod period) {
        LocalDate today = LocalDate.now();
        LocalDate dateStart;
        LocalDateTime dateTimeStart;
        LocalDateTime dateTimeEnd = today.plusDays(1).atStartOfDay();

        switch (period) {
            case DAY:
                dateStart = today;
                dateTimeStart = today.atStartOfDay();
                break;
            case WEEK:
                dateStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                dateTimeStart = dateStart.atStartOfDay();
                break;
            case YEAR:
                dateStart = today.with(TemporalAdjusters.firstDayOfYear());
                dateTimeStart = dateStart.atStartOfDay();
                break;
            default: // ALL
                dateStart = null;
                dateTimeStart = null;
                break;
        }

        long totalRestaurants;
        long approvedRestaurants;
        long pendingRestaurants;
        long restaurantStaff;
        long branchStaff;
        Long totalOrders;
        Double totalRevenue;

        if (dateStart == null) {
            // ALL — no date filter
            totalRestaurants = restaurantRepository.count();
            approvedRestaurants = restaurantRepository.countByApprovalStatus(ApprovalStatus.APPROVED);
            pendingRestaurants = restaurantRepository.countByApprovalStatus(ApprovalStatus.PENDING);
            restaurantStaff = restaurantUsersRepository.countAllStaff();
            branchStaff = branchUsersRepository.count();
            totalOrders = orderRepository.countPaidOrConfirmedOrders();
            totalRevenue = orderRepository.sumRevenuePaidOrConfirmedOrders();
        } else {
            LocalDate dateEnd = today;
            totalRestaurants = restaurantRepository.countRestaurantsByDateRange(dateStart, dateEnd);
            approvedRestaurants = restaurantRepository.countByApprovalStatusAndCreatedAtBetween(ApprovalStatus.APPROVED, dateStart, dateEnd);
            pendingRestaurants = restaurantRepository.countByApprovalStatusAndCreatedAtBetween(ApprovalStatus.PENDING, dateStart, dateEnd);
            restaurantStaff = restaurantUsersRepository.countStaffCreatedBetween(dateStart, dateEnd);
            branchStaff = branchUsersRepository.countStaffCreatedBetween(dateStart, dateEnd);
            totalOrders = orderRepository.countPaidOrConfirmedOrdersByDateRange(dateTimeStart, dateTimeEnd);
            totalRevenue = orderRepository.sumRevenuePaidOrConfirmedOrdersByDateRange(dateTimeStart, dateTimeEnd);
        }

        return SystemStatsDTO.builder()
                .period(period)
                .totalRestaurants(totalRestaurants)
                .approvedRestaurants(approvedRestaurants)
                .pendingRestaurants(pendingRestaurants)
                .restaurantStaff(restaurantStaff)
                .branchStaff(branchStaff)
                .totalStaffMembers(restaurantStaff + branchStaff)
                .totalOrders(totalOrders != null ? totalOrders : 0L)
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .build();
    }
}

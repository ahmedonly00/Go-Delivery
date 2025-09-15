package com.goDelivery.goDelivery.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.model.Branches;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.repository.BranchesRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;

@Service
public class BranchesService {

    private final BranchesRepository branchesRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;

    public BranchesService(BranchesRepository branchesRepository, RestaurantRepository restaurantRepository, RestaurantMapper restaurantMapper) {
        this.branchesRepository = branchesRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantMapper = restaurantMapper;
    }
    
    public BranchesDTO addBranchToRestaurant(Long restaurantId, BranchesDTO branchDTO) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        
        Branches branch = restaurantMapper.toBranch(branchDTO);
        branch.setRestaurant(restaurant);
        
        Branches savedBranch = branchesRepository.save(branch);
        return restaurantMapper.toBranchDTO(savedBranch);
    }

    public BranchesDTO getBranchById(Long branchId) {
        Branches branches = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        return restaurantMapper.toBranchDTO(branches);
    }

    public BranchesDTO updateBranch(Long branchId, BranchesDTO branchDTO) {
        Branches existingBranch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));

        // Update fields from DTO
        existingBranch.setBranchName(branchDTO.getBranchName());
        existingBranch.setAddress(branchDTO.getAddress());
        existingBranch.setLatitude(branchDTO.getLatitude());
        existingBranch.setLongitude(branchDTO.getLongitude());
        existingBranch.setPhoneNumber(branchDTO.getPhoneNumber());
        existingBranch.setOperatingHours(branchDTO.getOperatingHours());
        existingBranch.setActive(branchDTO.isActive());
        existingBranch.setUpdatedAt(java.time.LocalDate.now());

        Branches updatedBranch = branchesRepository.save(existingBranch);
        return restaurantMapper.toBranchDTO(updatedBranch);
    }

    public void deleteBranch(Long branchId) {
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        branchesRepository.delete(branch);
    }

    public List<BranchesDTO> getAllBranches() {
        List<Branches> branches = branchesRepository.findAll();
        return branches.stream()
                .map(restaurantMapper::toBranchDTO)
                .collect(Collectors.toList());
    }
}

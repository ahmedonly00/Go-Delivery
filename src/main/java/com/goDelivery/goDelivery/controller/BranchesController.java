package com.goDelivery.goDelivery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.service.BranchesService;
    
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/branches")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BranchesController {
    
    private static final Logger log = LoggerFactory.getLogger(BranchesController.class);

    
    private final BranchesService branchesService;

    
    @PostMapping("/addBranch/{restaurantId}")
    public ResponseEntity<BranchesDTO> addBranchToRestaurant(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long restaurantId,
            @Valid @RequestBody BranchesDTO branchDTO) {
        log.info("Adding branch details for branch {}", branchDTO.getBranchName());
        BranchesDTO createdBranch = branchesService.addBranchToRestaurant(restaurantId, branchDTO);
        return new ResponseEntity<>(createdBranch, HttpStatus.CREATED);
    }

    @GetMapping("/getBranches/{branchId}")
    public ResponseEntity<BranchesDTO> getBranchById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long branchId) {
        log.info("Getting branch details for branch {}", branchId);
        BranchesDTO branch = branchesService.getBranchById(branchId);
        return ResponseEntity.ok(branch);
    }

    @PutMapping("/updateBranch/{branchId}")
    public ResponseEntity<BranchesDTO> updateBranch(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long branchId,
            @Valid @RequestBody BranchesDTO branchDTO) {
        log.info("Updating branch details for branch {}", branchId);
        BranchesDTO updatedBranch = branchesService.updateBranch(branchId, branchDTO);
        return ResponseEntity.ok(updatedBranch);
    }

    @DeleteMapping("/deleteBranch/{branchId}")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long branchId) {
        log.info("Deleting branch details for branch {}", branchId);
        branchesService.deleteBranch(branchId);
        return ResponseEntity.noContent().build();
    }
}

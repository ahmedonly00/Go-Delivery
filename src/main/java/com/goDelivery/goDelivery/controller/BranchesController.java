package com.goDelivery.goDelivery.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/v1/branches")
public class BranchesController {

    
    @Autowired
    private BranchesService branchesService;

    
    @PostMapping("/restaurants/{restaurantId}/branches")
    public ResponseEntity<BranchesDTO> addBranchToRestaurant(
            @PathVariable Long restaurantId,
            @Valid @RequestBody BranchesDTO branchDTO) {
        BranchesDTO createdBranch = branchesService.addBranchToRestaurant(restaurantId, branchDTO);
        return new ResponseEntity<>(createdBranch, HttpStatus.CREATED);
    }

    @GetMapping("/branches/{branchId}")
    public ResponseEntity<BranchesDTO> getBranchById(@PathVariable Long branchId) {
        BranchesDTO branch = branchesService.getBranchById(branchId);
        return ResponseEntity.ok(branch);
    }

    @PutMapping("/branches/{branchId}")
    public ResponseEntity<BranchesDTO> updateBranch(
            @PathVariable Long branchId,
            @Valid @RequestBody BranchesDTO branchDTO) {
        BranchesDTO updatedBranch = branchesService.updateBranch(branchId, branchDTO);
        return ResponseEntity.ok(updatedBranch);
    }

    @DeleteMapping("/branches/{branchId}")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long branchId) {
        branchesService.deleteBranch(branchId);
        return ResponseEntity.noContent().build();
    }
}

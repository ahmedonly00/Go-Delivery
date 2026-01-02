package com.goDelivery.goDelivery.test;

import com.goDelivery.goDelivery.dto.branch.BranchSetupDTO;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.Enum.ApprovalStatus;

/**
 * Test class to verify imports are working correctly
 */
public class ImportTest {
    public static void main(String[] args) {
        // Test BranchSetupDTO
        BranchSetupDTO setupDTO = new BranchSetupDTO();
        setupDTO.setBranchName("Test Branch");
        System.out.println("BranchSetupDTO import works: " + setupDTO.getBranchName());
        
        // Test BranchesDTO
        BranchesDTO branchesDTO = new BranchesDTO();
        branchesDTO.setBranchName("Test DTO");
        System.out.println("BranchesDTO import works: " + branchesDTO.getBranchName());
        
        // Test ApprovalStatus
        ApprovalStatus status = ApprovalStatus.PENDING;
        System.out.println("ApprovalStatus import works: " + status);
    }
}

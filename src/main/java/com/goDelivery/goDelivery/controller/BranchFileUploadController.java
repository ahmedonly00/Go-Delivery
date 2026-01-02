package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.file.FileUploadResponse;
import com.goDelivery.goDelivery.service.BranchMenuUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file-upload/branches")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
@Tag(name = "Branch File Upload", description = "Branch file upload operations")
public class BranchFileUploadController {
    
    private final BranchMenuUploadService branchMenuUploadService;

    @PostMapping("/{branchId}/menu-upload")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Upload branch menu",
        description = "Upload menu file (PDF, Excel, or Image) for branch. The file will be processed using OCR to extract menu items."
    )
    public ResponseEntity<FileUploadResponse> uploadBranchMenu(
            @PathVariable Long branchId,
            @RequestParam("file") MultipartFile file) {
        
        log.info("Received menu upload request for branch ID: {}, file: {}", 
                branchId, file.getOriginalFilename());
        
        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(FileUploadResponse.builder()
                            .success(false)
                            .message("Please select a file to upload")
                            .build());
        }
        
        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(FileUploadResponse.builder()
                            .success(false)
                            .message("File size must be less than 10MB")
                            .build());
        }
        
        FileUploadResponse response = branchMenuUploadService.processBranchMenuUpload(file, branchId);
        
        if (response.isSuccess()) {
            log.info("Menu upload processed successfully for branch {}: {} items extracted", 
                    branchId, response.getMenuItems().size());
        } else {
            log.error("Menu upload failed for branch {}: {}", branchId, response.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}

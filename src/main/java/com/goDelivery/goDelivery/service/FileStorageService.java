package com.goDelivery.goDelivery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;
    
    @Value("${file.allowed-image-types:jpg,jpeg,png,gif}")
    private String allowedImageTypes;
    
    @Value("${file.allowed-document-types:pdf}")
    private String allowedDocumentTypes;
    
    private Set<String> allowedImageExtensions;
    private Set<String> allowedDocumentExtensions;
    
    @PostConstruct
    public void init() {
        // Initialize allowed image extensions
        allowedImageExtensions = new HashSet<>(Arrays.asList(allowedImageTypes.split(",")));
        
        // Initialize allowed document extensions
        allowedDocumentExtensions = new HashSet<>(Arrays.asList(allowedDocumentTypes.split(",")));
        
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(uploadDir).toAbsolutePath().normalize());
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    public String storeFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }
        
        try {
            // Clean the subdirectory path to prevent directory traversal
            String cleanSubDir = subDirectory.replaceAll("[/\\\\]+", "/")
                                          .replaceAll("^[./]+", "")  
                                          .replaceAll("[^a-zA-Z0-9/_.-]", "_");
            
            // Create the target directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, cleanSubDir).toAbsolutePath().normalize();
            
            // Security check: make sure the upload path is still within the intended directory
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!uploadPath.startsWith(basePath)) {
                throw new RuntimeException("Invalid file storage location");
            }
            
            Files.createDirectories(uploadPath);

            // Generate a unique filename with null safety
            String originalFileName = Optional.ofNullable(file.getOriginalFilename())
                .map(StringUtils::cleanPath)
                .orElse("");
                
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
            } else {
                // Default extension if none found
                fileExtension = "dat";
            }
            
            // Validate file extension based on subdirectory
            if (subDirectory.contains("logo") || subDirectory.contains("images") || subDirectory.contains("menu-items")) {
                if (!allowedImageExtensions.contains(fileExtension)) {
                    throw new RuntimeException("Invalid image file type. Allowed types: " + allowedImageTypes);
                }
            } else if (subDirectory.contains("documents")) {
                if (!allowedDocumentExtensions.contains(fileExtension)) {
                    throw new RuntimeException("Invalid document file type. Allowed types: " + allowedDocumentTypes);
                }
            }
            
            String fileName = UUID.randomUUID().toString() + "." + fileExtension;

            // Copy file to the target location
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return cleanSubDir + "/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename() + ". Please try again!", ex);
        }
    }
}

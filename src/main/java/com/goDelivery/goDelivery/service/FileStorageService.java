package com.goDelivery.goDelivery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }
        
        try {
            // Create the target directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir + subDirectory).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // Generate a unique filename with null safety
            String originalFileName = Optional.ofNullable(file.getOriginalFilename())
                .map(StringUtils::cleanPath)
                .orElse("");
                
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            } else {
                // Default extension if none found
                fileExtension = ".dat";
            }
            
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // Copy file to the target location
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return subDirectory + "/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename() + ". Please try again!", ex);
        }
    }
}

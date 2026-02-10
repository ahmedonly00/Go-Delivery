package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.file.FileUploadResponse;
import com.goDelivery.goDelivery.service.FileStorageService;
import com.goDelivery.goDelivery.service.MenuUploadService;
import com.goDelivery.goDelivery.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/file-upload/restaurants")
@CrossOrigin(origins = { "https://delivery.ivas.rw",
        "https://delivery.apis.ivas.rw" }, allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
                RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS }, allowCredentials = "true")
@Tag(name = "File Upload", description = "File upload management")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MenuUploadService menuUploadService;

    @Value("${app.base-url:https://delivery.apis.ivas.rw}")
    private String baseUrl;

    @PostMapping("/{restaurantId}/logo")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public ResponseEntity<?> uploadLogo(
            @PathVariable Long restaurantId,
            @RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\": \"Please select a file to upload\"}");
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("{\"error\": \"Only image files are allowed\"}");
            }

            // Store the file
            String filePath = fileStorageService.storeFile(file, "restaurants/" + restaurantId + "/logo");

            // Update restaurant logo URL with full URL
            String fullUrl = baseUrl + "/api/files/" + filePath.replace("\\", "/");
            restaurantService.updateRestaurantLogo(restaurantId, fullUrl);

            return ResponseEntity.ok().body("{\"filePath\": \"" + fullUrl + "\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Failed to upload file: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/{restaurantId}/promotions")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public ResponseEntity<String> uploadPromotion(
            @PathVariable Long restaurantId,
            @RequestParam("file") MultipartFile file) {
        String filePath = fileStorageService.storeFile(file, "restaurants/" + restaurantId + "/promotions");
        String fullUrl = baseUrl + "/api/files/" + filePath.replace("\\", "/");
        return ResponseEntity.ok().body("{\"filePath\": \"" + fullUrl + "\"}");
    }

    @PostMapping("/{restaurantId}/menu-upload")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public ResponseEntity<FileUploadResponse> uploadMenu(
            @PathVariable Long restaurantId,
            @RequestParam("file") MultipartFile file) {
        FileUploadResponse response = menuUploadService.processMenuUpload(file, restaurantId);
        return ResponseEntity.ok(response);
    }
}

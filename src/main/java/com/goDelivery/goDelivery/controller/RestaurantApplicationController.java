package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationRequest;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationResponse;
import com.goDelivery.goDelivery.service.RestaurantApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/restaurant-applications")
@RequiredArgsConstructor
public class RestaurantApplicationController {

    private final RestaurantApplicationService applicationService;

    @PostMapping
    public ResponseEntity<RestaurantApplicationResponse> submitApplication(
            @Valid @RequestBody RestaurantApplicationRequest request) {
        
        RestaurantApplicationResponse response = applicationService.submitApplication(request);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getApplicationId())
                .toUri();
                
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantApplicationResponse> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantApplicationResponse>> getAllApplications(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
                
        return ResponseEntity.ok(applicationService.getAllApplications(status, pageable));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{id}/review")
    public ResponseEntity<RestaurantApplicationResponse> reviewApplication(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantApplicationRequest request) {
                
        return ResponseEntity.ok(applicationService.reviewApplication(id, request));
    }

    @GetMapping("/status")
    public ResponseEntity<RestaurantApplicationResponse> checkStatus(
            @RequestParam String email) {
                
        return ResponseEntity.ok(applicationService.checkApplicationStatus(email));
    }
}

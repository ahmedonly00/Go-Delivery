package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationReviewRequest;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationResponse;
import com.goDelivery.goDelivery.service.RestaurantApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    private final RestaurantApplicationService applicationService;

    @GetMapping("/all")
    public ResponseEntity<Page<RestaurantApplicationResponse>> getAllApplications(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(applicationService.getAllApplications(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantApplicationResponse> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<RestaurantApplicationResponse> reviewApplication(
            @PathVariable Long id,
            @RequestBody RestaurantApplicationReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

    
        String adminEmail = userDetails.getUsername();
        return ResponseEntity.ok(applicationService.reviewApplication(id, request, adminEmail));
    }
}

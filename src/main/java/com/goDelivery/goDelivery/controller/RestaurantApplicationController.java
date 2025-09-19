package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.restaurant.CreateRestaurantApplicationRequest;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationReviewRequest;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantApplicationResponse;
import com.goDelivery.goDelivery.service.EmailService;
import com.goDelivery.goDelivery.service.RestaurantApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/restaurant-applications")
@RequiredArgsConstructor
public class RestaurantApplicationController {

    private final RestaurantApplicationService applicationService;

    private final EmailService emailService;

    @PostMapping(value = "/submit")
    public ResponseEntity<RestaurantApplicationResponse> submitApplication(
            @Valid @RequestBody CreateRestaurantApplicationRequest request) {
        
        RestaurantApplicationResponse response = applicationService.submitApplication(request);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getApplicationId())
                .toUri();
                
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<RestaurantApplicationResponse> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @GetMapping(value = "/all")
    public ResponseEntity<Page<RestaurantApplicationResponse>> getAllApplications(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
                
        return ResponseEntity.ok(applicationService.getAllApplications(status, pageable));
    }

   @PutMapping(value = "/{id}/review")
   public ResponseEntity<RestaurantApplicationResponse> reviewApplication(
           @PathVariable Long id,
           @Valid @RequestBody RestaurantApplicationReviewRequest request,
           @AuthenticationPrincipal UserDetails userDetails) {

       String adminEmail = userDetails.getUsername();
       return ResponseEntity.ok(applicationService.reviewApplication(id, request, adminEmail));
   }

    @GetMapping(value = "/status")
    public ResponseEntity<RestaurantApplicationResponse> checkStatus(
            @RequestParam String email) {
                
        return ResponseEntity.ok(applicationService.checkApplicationStatus(email));
    }

    @GetMapping("/test-email")
    public ResponseEntity<String> testEmail(@RequestParam String to) {
        try {
            emailService.sendTestEmail(to);
            return ResponseEntity.ok("Test email sent to " + to);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Email sending failed: " + e.getMessage());
        }
    }

}

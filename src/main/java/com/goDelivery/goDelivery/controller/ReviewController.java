package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.review.CreateReviewRequest;
import com.goDelivery.goDelivery.dtos.review.ReviewResponseDTO;
import com.goDelivery.goDelivery.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponseDTO review = reviewService.createReview(request);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ReviewResponseDTO>> getRestaurantReviews(
            @PathVariable Long restaurantId) {
        List<ReviewResponseDTO> reviews = reviewService.getRestaurantReviews(restaurantId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/biker/{bikerId}")
    public ResponseEntity<List<ReviewResponseDTO>> getBikerReviews(
            @PathVariable Long bikerId) {
        List<ReviewResponseDTO> reviews = reviewService.getBikerReviews(bikerId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') and #customerId == principal.id")
    public ResponseEntity<List<ReviewResponseDTO>> getCustomerReviews(
            @PathVariable Long customerId) {
        List<ReviewResponseDTO> reviews = reviewService.getCustomerReviews(customerId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/{reviewId}/helpful")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markReviewAsHelpful(@PathVariable Long reviewId) {
        reviewService.markReviewAsHelpful(reviewId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> verifyReview(@PathVariable Long reviewId) {
        reviewService.verifyReview(reviewId);
        return ResponseEntity.ok().build();
    }
}

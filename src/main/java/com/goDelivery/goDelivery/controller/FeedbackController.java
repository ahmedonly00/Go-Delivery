package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.feedback.FeedbackRequest;
import com.goDelivery.goDelivery.dtos.feedback.FeedbackResponse;
import com.goDelivery.goDelivery.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/submit")
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @Valid @RequestBody FeedbackRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long customerId = Long.parseLong(userDetails.getUsername());
        FeedbackResponse response = feedbackService.submitFeedback(request, customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-feedback")
    public ResponseEntity<List<FeedbackResponse>> getMyFeedback(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long customerId = Long.parseLong(userDetails.getUsername());
        List<FeedbackResponse> feedbackList = feedbackService.getFeedbackByCustomer(customerId);
        return ResponseEntity.ok(feedbackList);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<FeedbackResponse>> getOrderFeedback(@PathVariable Long orderId) {
        List<FeedbackResponse> feedbackList = feedbackService.getFeedbackByOrder(orderId);
        return ResponseEntity.ok(feedbackList);
    }
}

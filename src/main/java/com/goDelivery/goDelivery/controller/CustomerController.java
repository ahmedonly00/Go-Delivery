package com.goDelivery.goDelivery.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goDelivery.goDelivery.dtos.customer.CustomerRegistrationRequest;
import com.goDelivery.goDelivery.dtos.customer.CustomerResponse;
import com.goDelivery.goDelivery.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerCustomer(
            @Valid @RequestBody CustomerRegistrationRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        
        try {
            CustomerResponse customerResponse = customerService.registerCustomer(request);
            log.info("Successfully registered user with email: {}", request.getEmail());
            
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "Registration successful. Please check your email for the OTP to verify your account.");
            successResponse.put("data", customerResponse);
            successResponse.put("requiresVerification", true);
            return ResponseEntity.ok(successResponse);
            
        } catch (Exception e) {
            log.error("Error during registration for email: {}", request.getEmail(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/profile/{email}")
    public ResponseEntity<CustomerResponse> getCustomerProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String email) {
        CustomerResponse customerResponse = customerService.getCustomerProfile(email);
        return ResponseEntity.ok(customerResponse);
    }

    @GetMapping("/getAllCustomers")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> customerResponses = customerService.getAllCustomers();
        return ResponseEntity.ok(customerResponses);
    }

    @GetMapping("/getCustomerByEmail/{email}")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(String email,
            @AuthenticationPrincipal UserDetails userDetails) {

        CustomerResponse customerResponse = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(customerResponse);
    }
    
}

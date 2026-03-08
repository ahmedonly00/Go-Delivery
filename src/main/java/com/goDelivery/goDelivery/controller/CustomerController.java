package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.customer.CustomerRegistrationRequest;
import com.goDelivery.goDelivery.dtos.customer.CustomerResponse;
import com.goDelivery.goDelivery.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Customer", description = "Customer registration and profile management")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register")
    @Operation(summary = "Register a new customer", description = "Creates a customer account and sends a verification OTP to the provided email.")
    public ResponseEntity<CustomerResponse> registerCustomer(
            @Valid @RequestBody CustomerRegistrationRequest request) {

        log.info("Registering new customer with email: {}", request.getEmail());
        CustomerResponse response = customerService.registerCustomer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current customer profile", description = "Returns the profile of the currently authenticated customer.")
    public ResponseEntity<CustomerResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Fetching profile for customer: {}", userDetails.getUsername());
        return ResponseEntity.ok(customerService.getCustomerProfile(userDetails.getUsername()));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(customerService.getCustomerById(customerId));
    }

    @PutMapping("/{customerId}")
    @Operation(summary = "Update customer", description = "Updates the customer's profile information.")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerRegistrationRequest request) {

        log.info("Updating customer with ID: {}", customerId);
        return ResponseEntity.ok(customerService.updateCustomer(customerId, request));
    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete customer", description = "Permanently deletes a customer account. Super admin only.")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable Long customerId) {

        log.info("Deleting customer with ID: {}", customerId);
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all customers", description = "Returns all registered customers. Super admin only.")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {

        return ResponseEntity.ok(customerService.getAllCustomers());
    }
}

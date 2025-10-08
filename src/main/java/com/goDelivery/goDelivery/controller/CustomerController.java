package com.goDelivery.goDelivery.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goDelivery.goDelivery.dtos.customer.CustomerRegistrationRequest;
import com.goDelivery.goDelivery.dtos.customer.CustomerResponse;
import com.goDelivery.goDelivery.service.CustomerService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/registerCustomer")
    public ResponseEntity<CustomerResponse> registerCustomer(@Valid @RequestBody CustomerRegistrationRequest customerRegistrationRequest) {
        System.out.println("Received registration request: " + customerRegistrationRequest);
        if (customerRegistrationRequest == null) {
            System.out.println("Request body is null");
            return ResponseEntity.badRequest().build();
        }
        CustomerResponse customerResponse = customerService.registerCustomer(customerRegistrationRequest);
        return ResponseEntity.ok(customerResponse);
        
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

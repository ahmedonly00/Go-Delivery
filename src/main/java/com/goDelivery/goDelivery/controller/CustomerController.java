package com.goDelivery.goDelivery.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goDelivery.goDelivery.dtos.customer.CustomerRegistrationRequest;
import com.goDelivery.goDelivery.dtos.customer.CustomerResponse;
import com.goDelivery.goDelivery.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/registerCustomer")
    public ResponseEntity<CustomerResponse> registerCustomer(CustomerRegistrationRequest customerRegistrationRequest) {
        if (customerRegistrationRequest == null) {
            return ResponseEntity.badRequest().build();
        }
        CustomerResponse customerResponse = customerService.registerCustomer(customerRegistrationRequest);
        return ResponseEntity.ok(customerResponse);
        
    }

    @GetMapping("/profile/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerProfile(Long customerId) {
        CustomerResponse customerResponse = customerService.getCustomerProfile(customerId);
        return ResponseEntity.ok(customerResponse);
    }

    @GetMapping("/getAllCustomers")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> customerResponses = customerService.getAllCustomers();
        return ResponseEntity.ok(customerResponses);
    }

    @GetMapping("/getCustomerByEmail/{email}")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(String email) {
        CustomerResponse customerResponse = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(customerResponse);
    }
    
}

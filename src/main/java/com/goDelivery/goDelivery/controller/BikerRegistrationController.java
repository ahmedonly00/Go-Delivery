package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.biker.BikerRegistrationRequest;
import com.goDelivery.goDelivery.dtos.biker.BikerRegistrationResponse;
import com.goDelivery.goDelivery.service.BikerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bikers")
@RequiredArgsConstructor
public class BikerRegistrationController {

    private final BikerService bikerService;

    @PostMapping("/register")
    public ResponseEntity<BikerRegistrationResponse> registerBiker(
            @Valid @RequestBody BikerRegistrationRequest request) {
        BikerRegistrationResponse response = bikerService.registerBiker(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

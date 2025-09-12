package com.goDelivery.goDelivery.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goDelivery.goDelivery.dtos.auth.LoginRequest;
import com.goDelivery.goDelivery.dtos.auth.LoginResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService service;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request){
        LoginResponse response = service.authenticate(request);
        return ResponseEntity.ok(response);
    }

}

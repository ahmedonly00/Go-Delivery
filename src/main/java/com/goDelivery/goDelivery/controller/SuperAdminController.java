package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.admin.CreateSuperAdminRequest;
import com.goDelivery.goDelivery.model.SuperAdmin;
import com.goDelivery.goDelivery.service.SuperAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") 
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    @PostMapping(value = "/registerSuperAdmin")
    public ResponseEntity<SuperAdmin> registerSuperAdmin(
            @Valid @RequestBody CreateSuperAdminRequest request) {
        
        SuperAdmin superAdmin = superAdminService.createSuperAdmin(request);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(superAdmin.getAdminId())
                .toUri();
                
        return ResponseEntity.created(location).body(superAdmin);
    }

    @GetMapping(value = "/all")
    public ResponseEntity<List<SuperAdmin>> getAllSuperAdmins() {
        return ResponseEntity.ok(superAdminService.getAllSuperAdmins());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<SuperAdmin> getSuperAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(superAdminService.getSuperAdminById(id));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<SuperAdmin> updateSuperAdmin(@PathVariable Long id, @RequestBody SuperAdmin superAdmin) {
        return ResponseEntity.ok(superAdminService.updateSuperAdmin(id, superAdmin));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteSuperAdmin(@PathVariable Long id) {
        superAdminService.deleteSuperAdmin(id);
        return ResponseEntity.ok().build();
    }  

    
}

package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.dtos.admin.CreateSuperAdminRequest;
import com.goDelivery.goDelivery.exception.DuplicateResourceException;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.SuperAdmin;
import com.goDelivery.goDelivery.repository.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuperAdminService {

    private final SuperAdminRepository superAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SuperAdmin createSuperAdmin(CreateSuperAdminRequest request) {
        // Check if email is already in use
        if (superAdminRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use");
        }

        // Create new super admin
        SuperAdmin superAdmin = SuperAdmin.builder()
                .fullNames(request.getFullNames())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Roles.SUPER_ADMIN)
                .isActive(true)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        // Save and return the new super admin
        return superAdminRepository.save(superAdmin);
    }

    public List<SuperAdmin> getAllSuperAdmins() {
        return superAdminRepository.findAll();
    }

    public SuperAdmin getSuperAdminById(Long id) {
        return superAdminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SuperAdmin not found with id: " + id));
    }

    public SuperAdmin updateSuperAdmin(Long id, SuperAdmin superAdmin) {
        SuperAdmin existingSuperAdmin = getSuperAdminById(id);
        existingSuperAdmin.setFullNames(superAdmin.getFullNames());
        existingSuperAdmin.setEmail(superAdmin.getEmail());
        existingSuperAdmin.setRole(superAdmin.getRole());
        existingSuperAdmin.setActive(true);
        existingSuperAdmin.setUpdatedAt(LocalDate.now());
        return superAdminRepository.save(existingSuperAdmin);
    }

    public void deleteSuperAdmin(Long id) {
        superAdminRepository.deleteById(id);
    } 
}

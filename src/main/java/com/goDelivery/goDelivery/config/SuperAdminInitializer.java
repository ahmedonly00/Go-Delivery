package com.goDelivery.goDelivery.config;

import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.model.SuperAdmin;
import com.goDelivery.goDelivery.repository.SuperAdminRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminInitializer {

    private final SuperAdminRepository superAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.superadmin.email:ndayizeye.ahmedy@gmail.com}")
    private String defaultAdminEmail;

    @Value("${app.superadmin.password:Admin@123}")
    private String defaultAdminPassword;

    @PostConstruct
    public void init() {
        // Check if any super admin exists
        if (superAdminRepository.count() == 0) {
            // Create default super admin
            SuperAdmin superAdmin = SuperAdmin.builder()
                    .fullNames("System Administrator")
                    .email(defaultAdminEmail)
                    .password(passwordEncoder.encode(defaultAdminPassword))
                    .role(Roles.SUPER_ADMIN)
                    .isActive(true)
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            superAdminRepository.save(superAdmin);
            log.info("Default super admin created with email: {}", defaultAdminEmail);
        }
    }
}

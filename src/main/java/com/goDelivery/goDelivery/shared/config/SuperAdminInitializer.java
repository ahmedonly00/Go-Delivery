package com.goDelivery.goDelivery.shared.config;

import com.goDelivery.goDelivery.shared.enums.Roles;
import com.goDelivery.goDelivery.modules.restaurant.model.SuperAdmin;
import com.goDelivery.goDelivery.modules.restaurant.repository.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminInitializer {

    private final SuperAdminRepository superAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.superadmin.email}")
    private String defaultAdminEmail;

    @Value("${app.superadmin.password}")
    private String defaultAdminPassword;

    @Bean
    public ApplicationRunner initSuperAdmin() {
        return args -> {
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
            log.info("Default super admin created: {}", defaultAdminEmail);
        } else {
            log.info("Super admin already exists");
        }
        };
    }
}

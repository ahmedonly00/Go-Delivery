package com.goDelivery.goDelivery.configSecurity;

import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.model.SuperAdmin;
import com.goDelivery.goDelivery.repository.CustomerRepository;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.repository.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final RestaurantUsersRepository restaurantUsersRepository;
    private final SuperAdminRepository superAdminRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        
        // First try to find in RestaurantUsers
        Optional<RestaurantUsers> restaurantUser = restaurantUsersRepository.findByEmail(username);
        if (restaurantUser.isPresent()) {
            RestaurantUsers user = restaurantUser.get();
            log.info("Found restaurant user: {}, Role: {}, Active: {}", user.getEmail(), user.getRole(), user.isActive());
            return user;
        }
        
        // If not found, try to find in SuperAdmin
        Optional<SuperAdmin> superAdmin = superAdminRepository.findByEmail(username);
        if (superAdmin.isPresent()) {
            SuperAdmin admin = superAdmin.get();
            log.info("Found super admin: {}, Role: {}", admin.getEmail(), admin.getRole());
            return new User(admin.getEmail(), admin.getPassword(), getAuthorities(admin.getRole()));
        }
        
        // If not found, try to find in Customer
        Optional<Customer> customer = customerRepository.findByEmail(username);
        if (customer.isPresent()) {
            Customer cust = customer.get();
            log.info("Found customer: {}, Active: {}", cust.getEmail(), cust.getIsActive());
            return new User(cust.getEmail(), cust.getPassword(), getAuthorities(cust.getRoles()));
        }
        
        log.error("User not found with email: {}", username);
        throw new UsernameNotFoundException("User not found with email: " + username);
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(Enum<?> role) {
        if (role == null) {
            log.warn("Role is null, assigning default ROLE_USER");
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        String roleName = "ROLE_" + role.name();
        log.debug("Creating authority with role: {} (from enum: {})", roleName, role);
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }
    
    public void updatePassword(UserDetails user, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        // Update password in the appropriate repository
        restaurantUsersRepository.findByEmail(user.getUsername())
                .ifPresent(u -> {
                    u.setPassword(encodedPassword);
                    restaurantUsersRepository.save(u);
                });
        
        superAdminRepository.findByEmail(user.getUsername())
                .ifPresent(admin -> {
                    admin.setPassword(encodedPassword);
                    superAdminRepository.save(admin);
                });
        
        customerRepository.findByEmail(user.getUsername())
                .ifPresent(customer -> {
                    customer.setPassword(encodedPassword);
                    customerRepository.save(customer);
                });
    }
}

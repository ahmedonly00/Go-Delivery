package com.goDelivery.goDelivery.configSecurity;

import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.model.SuperAdmin;
import com.goDelivery.goDelivery.repository.CustomerRepository;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.repository.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final RestaurantUsersRepository restaurantUsersRepository;
    private final SuperAdminRepository superAdminRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Loading user by username: " + username);
        
        // First try to find in RestaurantUsers
        Optional<RestaurantUsers> restaurantUser = restaurantUsersRepository.findByEmail(username);
        if (restaurantUser.isPresent()) {
            RestaurantUsers user = restaurantUser.get();
            System.out.println("Found restaurant user: " + user.getEmail() + ", Role: " + user.getRole() + ", Active: " + user.isActive());
            return createUserDetails(user.getEmail(), user.getPassword(), user.getRole(), user.isActive(), "RestaurantUser");
        }
        
        // If not found, try to find in SuperAdmin
        Optional<SuperAdmin> superAdmin = superAdminRepository.findByEmail(username);
        if (superAdmin.isPresent()) {
            SuperAdmin admin = superAdmin.get();
            System.out.println("Found super admin: " + admin.getEmail() + ", Role: " + admin.getRole());
            return createUserDetails(admin.getEmail(), admin.getPassword(), admin.getRole(), true, "SuperAdmin");
        }
        
        // If not found, try to find in Customer
        Optional<Customer> customer = customerRepository.findByEmail(username);
        if (customer.isPresent()) {
            Customer cust = customer.get();
            System.out.println("Found customer: " + cust.getEmail() + ", Active: " + cust.getIsActive());
            return createUserDetails(cust.getEmail(), cust.getPassword(), cust.getRoles(), cust.getIsActive(), "Customer");
        }
        
        System.err.println("User not found with email: " + username);
        throw new UsernameNotFoundException("User not found with email: " + username);
    }
    
    private UserDetails createUserDetails(String email, String password, Enum<?> role, boolean isActive, String userType) {
        System.out.println("Creating UserDetails for: " + email + " (" + userType + ")");
        System.out.println("  - Role enum: " + role);
        System.out.println("  - Is Active: " + isActive);
        
        Collection<? extends GrantedAuthority> authorities = getAuthorities(role);
        System.out.println("  - Granted Authorities: " + authorities);
        
        UserDetails userDetails = User.builder()
                .username(email)
                .password(password)
                .authorities(authorities)
                .disabled(!isActive)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
                
        System.out.println("Created UserDetails: " + userDetails.getUsername() + 
                          ", Authorities: " + userDetails.getAuthorities() + 
                          ", Enabled: " + userDetails.isEnabled() +
                          ", Account Non Expired: " + userDetails.isAccountNonExpired() +
                          ", Account Non Locked: " + userDetails.isAccountNonLocked() +
                          ", Credentials Non Expired: " + userDetails.isCredentialsNonExpired());
        
        return userDetails;
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(Enum<?> role) {
        if (role == null) {
            System.out.println("Role is null, assigning default ROLE_USER");
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        String roleName = "ROLE_" + role.name();
        System.out.println("Creating authority with role: " + roleName + " (from enum: " + role + ")");
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

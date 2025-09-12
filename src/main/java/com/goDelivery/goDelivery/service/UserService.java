package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;


@Service
public class UserService {

    @Autowired
    private RestaurantUsersRepository restaurantUsersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public Page<RestaurantUsers> getAllUsers(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return restaurantUsersRepository.findAll(pageable);
    }

    public Optional<RestaurantUsers> getUserById(Long id) {
        return restaurantUsersRepository.findById(id);
    }

    public RestaurantUsers findByUser(String email) {
        return restaurantUsersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public String registerUser(RestaurantUsers user) {
        try {
            // Set additional user properties
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setCreatedAt(LocalDate.now());
            user.setUpdatedAt(LocalDate.now());
            user.setActive(true);
            
            // Save the user
            restaurantUsersRepository.save(user);
            return "User registered successfully!";
        } catch (Exception e) {
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
        }
    }

    public RestaurantUsers updateUser(Long id, RestaurantUsers userDetails) {
        return restaurantUsersRepository.findById(id)
                .map(user -> {
                    user.setEmail(userDetails.getEmail());
                    if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                    }
                    user.setRole(userDetails.getRole());
                    user.setUpdatedAt(LocalDate.now());
                    return restaurantUsersRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void deleteUser(Long id) {
        if (restaurantUsersRepository.existsById(id)) {
            restaurantUsersRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }

    public RestaurantUsers createUser(RestaurantUsers user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDate.now());
        user.setUpdatedAt(LocalDate.now());
        user.setActive(true);
        return restaurantUsersRepository.save(user);
    }
}

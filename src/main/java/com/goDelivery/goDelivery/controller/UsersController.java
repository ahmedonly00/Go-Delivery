package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.user.RestaurantUserRequest;
import com.goDelivery.goDelivery.dtos.user.RestaurantUserResponse;
import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UsersController {

    private final UsersService usersService;

    @PostMapping(value = "/createUser/{restaurantId}")
    public ResponseEntity<RestaurantUserResponse> createUser(
            @PathVariable Long restaurantId,
            @Valid @RequestBody RestaurantUserRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();
        request.setRestaurantId(restaurantId);
        RestaurantUserResponse response = usersService.createUser(request, adminEmail);
        return ResponseEntity
                .created(URI.create("/api/v1/restaurant-admin/restaurants/" + restaurantId + "/users/" + response.getUserId()))
                .body(response);
    }

    @PutMapping(value = "/updateUser/{userId}")
    public ResponseEntity<RestaurantUserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody RestaurantUserRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();
        RestaurantUserResponse response = usersService.updateUser(userId, request, adminEmail);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/deleteUser/{userId}")
    public ResponseEntity<Void> deleteUser( @PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();
        usersService.deleteUser(userId, adminEmail);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/getUserById/{userId}")
    public ResponseEntity<RestaurantUserResponse> getUserById( @PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();
        RestaurantUserResponse response = usersService.getUserById(userId, adminEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/getAllUsersByRestaurant/{restaurantId}")
    public ResponseEntity<List<RestaurantUserResponse>> getAllUsersByRestaurant(
        @PathVariable Long restaurantId, @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();
        List<RestaurantUserResponse> userList = usersService.getAllUsersByRestaurant(restaurantId, adminEmail);
        return ResponseEntity.ok(userList);
    }

    @GetMapping(value = "/getActiveUsersByRestaurant/{restaurantId}")
    public ResponseEntity<List<RestaurantUserResponse>> getActiveUsersByRestaurant(
            @PathVariable Long restaurantId, @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();
        List<RestaurantUserResponse> userList = usersService.getActiveUsersByRestaurant(restaurantId, adminEmail);
        return ResponseEntity.ok(userList);
    }

    @PutMapping(value = "/updateUserRole/{userId}/role/{role}")
    public ResponseEntity<RestaurantUserResponse> updateUserRole(
            @PathVariable Long userId,
            @PathVariable Roles role, @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();
        RestaurantUserResponse response = usersService.updateUserRole(userId, role, adminEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/deactivateUser/{userId}")
    public ResponseEntity<RestaurantUserResponse> deactivateUser(
            @PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();
        RestaurantUserResponse response = usersService.deactivateUser(userId, adminEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/activateUser/{userId}")
    public ResponseEntity<RestaurantUserResponse> activateUser(
            @PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();
        RestaurantUserResponse response = usersService.activateUser(userId, adminEmail);
        return ResponseEntity.ok(response);
    }
}

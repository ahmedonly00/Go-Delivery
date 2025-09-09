package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private RestaurantUsersRepository restaurantUsersRepository;

    @Autowired
    private UserService userService;

    private RestaurantUsers testUser;
    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        testRestaurant = new Restaurant();
        testRestaurant.setRestaurantId(1L);
        testRestaurant.setRestaurantName("Test Restaurant");

        testUser = new RestaurantUsers();
        testUser.setUserId(1L);
        testUser.setFullNames("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setPhoneNumber("1234567890");
        testUser.setRole(Roles.RESTAURANT_ADMIN);
        testUser.setPermissions("");
        testUser.setActive(true);
        testRestaurant.setRestaurantId(1L);
        testUser.setRestaurant(testRestaurant);
    }

    @Test
    void createUser_ShouldSaveUser() {
        // Arrange
        when(restaurantUsersRepository.save(any(RestaurantUsers.class))).thenReturn(testUser);

        // Act
        userService.createUser(testUser);

        // Assert
        verify(restaurantUsersRepository).save(any(RestaurantUsers.class));
    }
}

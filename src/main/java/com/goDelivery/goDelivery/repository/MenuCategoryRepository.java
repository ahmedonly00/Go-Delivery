package com.goDelivery.goDelivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goDelivery.goDelivery.model.MenuCategory;
import com.goDelivery.goDelivery.model.Restaurant;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    Optional<MenuCategory> findByCategoryId(Long categoryId);
    Optional<MenuCategory> findByCategoryName(String categoryName);
    
    List<MenuCategory> findByRestaurant(Restaurant restaurant);
    
    default List<MenuCategory> findByRestaurantId(Long restaurantId) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        return findByRestaurant(restaurant);
    }
}

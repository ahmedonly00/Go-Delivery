package com.goDelivery.goDelivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goDelivery.goDelivery.model.MenuItem;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Optional<MenuItem> findByMenuItemId(Long itemId);
    Optional<MenuItem> findByMenuItemName(String itemName);
    List<MenuItem> findByRestaurant_RestaurantId(Long restaurantId);
    List<MenuItem> findByRestaurant_RestaurantIdAndIsAvailableTrue(Long restaurantId);
    List<MenuItem> findByRestaurant_RestaurantIdAndCategory_CategoryId(Long restaurantId, Long categoryId);
    List<MenuItem> findByRestaurant_RestaurantIdAndCategory_CategoryIdAndIsAvailableTrue(Long restaurantId, Long categoryId);
}
